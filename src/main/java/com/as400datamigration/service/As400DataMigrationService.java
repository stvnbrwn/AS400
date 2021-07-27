package com.as400datamigration.service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.as400datamigration.audit.AuditMessage;
import com.as400datamigration.audit.BatchDetailStatus;
import com.as400datamigration.audit.TableStatus;
import com.as400datamigration.common.Utility;
import com.as400datamigration.model.BatchDetail;
import com.as400datamigration.model.FailedBatchDetails;
import com.as400datamigration.model.SQLColumn;
import com.as400datamigration.model.TableMetaData;
import com.as400datamigration.model.TableProcess;
import com.as400datamigration.model.TableProcessDetail;
import com.as400datamigration.reposistory.As400Dao;
import com.as400datamigration.reposistory.PostgresDao;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class As400DataMigrationService {

	@Value("${batch.size}")
	private int batchSize;

	@Value("${max.attempt}")
	private int maxAttempt;

	@Autowired
	Utility utility;

	@Autowired
	As400Dao as400Dao;

	@Autowired
	PostgresDao postgresDao;

	@Value("${completeMigration}")
	private boolean completeMigrationFlag;

	@Async("ThreadExecutor")
	public void processCompleteMigration(TableMetaData tableMetaData, TableStatus tableStatus) {
		String tableName = tableMetaData.getTableName();
		try {
			if (tableStatus.equals(TableStatus.TABLE_NOT_CREATED))
				tableMetaData = createTable(tableMetaData.getTableName());
			if (tableStatus.equals(TableStatus.TABLE_NOT_FOUND_AT_SOURCE)
					|| tableStatus.equals(TableStatus.TABLE_DESC_NOT_FOUND_AT_SOURCE)
					|| tableStatus.equals(TableStatus.TABLE_CREATION_FAILED))
				createTableFromSync(tableMetaData, tableStatus);

			if (Objects.nonNull(tableMetaData) && Objects.nonNull(tableMetaData.getColumns())) {
				boolean allBatchInsert = performReadWriteOnTable(tableMetaData);
				if (allBatchInsert) {
					postgresDao.updateTableProcessStatus(
							new TableProcess(tableMetaData.getTableName(), TableStatus.MIGRATION_SUCCESSFUL)
									.getUpdateObjArray());
				} else {
					postgresDao.updateTableProcessStatus(
							new TableProcess(tableMetaData.getTableName(), TableStatus.MIGRATION_FAILED)
									.getUpdateObjArray());
				}
			}
		} catch (DuplicateKeyException e) {
			log.error(tableName + " Table migration is already performed.");
			log.error("Please select option 2 from main menu for syncing the additional data for this table.",e);
			
		} catch (Exception e) {
			log.error("Please check connection or " + tableName + " already performed");
			log.error(AuditMessage.EXECPTION_MSG + " May be columns meta data not found \n"
					+ "Error at processCompleteMigration method", e);
		}
	}

	private boolean performReadWriteOnTable(TableMetaData tableMetaData) {
		boolean allBatchInsert = true;
		log.info("Start performReadWriteOnTable execution ..!");
		if (tableMetaData.getTotalRows() > 0) {
			long maxRrn = tableMetaData.getMaxRrn();

			while (tableMetaData.getMinRrn() <= maxRrn) {
				tableMetaData.setMaxRrn(tableMetaData.getMinRrn() + batchSize - 1);
				if (tableMetaData.getMaxRrn() >= maxRrn)
					break;
				allBatchInsert = batchOpration(tableMetaData, allBatchInsert);
				tableMetaData.setMinRrn(tableMetaData.getMinRrn() + batchSize);
				if (!completeMigrationFlag) {
					log.info("End performReadWriteOnTable execution ..!");
					return allBatchInsert;
				}
			}
			if (tableMetaData.getMinRrn() > maxRrn) {
				tableMetaData.setMinRrn(tableMetaData.getMinRrn() - batchSize);
			}
			tableMetaData.setMaxRrn(maxRrn);
			allBatchInsert = batchOpration(tableMetaData, allBatchInsert);
		}
		log.info("End performReadWriteOnTable execution ..!");
		return allBatchInsert;
	}

	private boolean batchOpration(TableMetaData tableMetaData, boolean allBatchInsert) {
		log.info("Start batchOpration execution..! ");
		List<Object[]> tableData = null;
		tableMetaData.setBatchDetail(new BatchDetail(tableMetaData));
		tableData = as400Dao.readOprationOnTable(tableMetaData);
		System.out.println();
		if (Objects.nonNull(tableData)) {
			boolean batchInsert = postgresDao.writeOpraionOnTable(tableMetaData, tableData);
			allBatchInsert = allBatchInsert && batchInsert;
		} else {
			allBatchInsert = false;
		}
		log.info("End batchOpration execution..! ");
		return allBatchInsert;
	}

	@Transactional
	private TableMetaData createTable(String tableName) {
		log.info("Start createTable execution ...! ");
		TableMetaData tableMetaData = null;
		try {
			tableMetaData = as400Dao.getTableMetaData(tableName, true);
			if (Objects.nonNull(tableMetaData)) {
				tableMetaData.setTableName(tableName);
				List<SQLColumn> columns = as400Dao.getTableDesc(tableMetaData, true);
				if (Objects.nonNull(columns)) {
					tableMetaData.setColumns(columns);

					tableMetaData.setPostgresQueries(utility.getPostgresQueries(tableMetaData));

					postgresDao.createTable(tableMetaData);

					if (tableMetaData.getTotalRows() > 0) {
						tableMetaData.getTableProcess().setStatus(TableStatus.MIGRATION_PROCESS_IN_RUNNING);
					} else {
						tableMetaData.getTableProcess().setStatus(TableStatus.TABLE_CREATED_WITH_NO_DATA);
					}
					postgresDao.saveIntoTableProcess(tableMetaData.getTableProcess().getSaveObjArray());
				}
			}

		} catch (Exception e) {
			log.error("Exception At createTable !!! table Name :" + tableName , e);
			TableProcess tableProcess = new TableProcess(tableName);
			tableProcess.setTotalRows(tableMetaData.getTotalRows());
			tableProcess.setStatus(TableStatus.TABLE_CREATION_FAILED);
			postgresDao.saveIntoTableProcess(tableProcess.getSaveObjArray());
			postgresDao.saveIntoTableProcessDetail(new TableProcessDetail(tableName,
					AuditMessage.TABLE_CREATION_FAILED_MSG + AuditMessage.EXECPTION_MSG + e).getSaveObjArray());
		}
		log.info("End createTable execution ...! ");
		return tableMetaData;
	}

	public void processSyncInsert(String tableName) {
		TableMetaData tableMetaDataSource = new TableMetaData(tableName);
		try {
			TableProcess tableProcessDestination = postgresDao.getTableMetaData(tableName);
			if (Objects.nonNull(tableProcessDestination)) {
				tableMetaDataSource = as400Dao.getTableMetaData(tableName, false); // source pr dono bar ni mili
				// nhi mili to source s delete kar
				// di
				if (Objects.nonNull(tableMetaDataSource)) {
					tableMetaDataSource.setTableName(tableName);
					if (tableProcessDestination.getStatus().equals(TableStatus.TABLE_NOT_FOUND_AT_SOURCE)
							|| tableProcessDestination.getStatus().equals(TableStatus.TABLE_DESC_NOT_FOUND_AT_SOURCE)
							|| tableProcessDestination.getStatus().equals(TableStatus.TABLE_CREATION_FAILED)) {
						processCompleteMigration(tableMetaDataSource, tableProcessDestination.getStatus());
					} else {
						// no need safety purpose
						BatchDetail lastBatchDetail = postgresDao.getlastBatchDetails(tableName);
						if (Objects.nonNull(lastBatchDetail) && Objects.nonNull(tableProcessDestination.getColumnJson())
								&& !tableProcessDestination.getColumnJson().isEmpty()) {
							// ||tableMetaDataSource.getTableProcess().getMaxRrn() >
							// tableProcessDestination.getMaxRrn()
							if (tableMetaDataSource.getTableProcess().getMaxRrn() > lastBatchDetail.getEndingRrn()) {
								List<SQLColumn> columns = new ObjectMapper().readValue(
										tableProcessDestination.getColumnsJson(), new TypeReference<List<SQLColumn>>() {
										});
								boolean isSynced = false;
								if (Objects.nonNull(columns)) {
									isSynced = performReadWriteOnTable(new TableMetaData(
											tableMetaDataSource.getTableName(), tableMetaDataSource.getTotalRows(),
											lastBatchDetail.getEndingRrn() + 1, // minRrn
																				// from
																				// destination
											tableMetaDataSource.getMaxRrn(), columns));
								}
								if (isSynced) {
									tableMetaDataSource.getTableProcess()
											.setStatus(TableStatus.MIGRATION_SYNC_SUCCESSFUL);
									postgresDao.updateTableDeatil(tableMetaDataSource.getTableProcess()
											.getTableDetailsWithoutColumnsObjArray(), false);
								} else {
									tableMetaDataSource.getTableProcess().setStatus(TableStatus.MIGRATION_SYNC_FAIL);
									postgresDao.updateTableDeatil(tableMetaDataSource.getTableProcess()
											.getTableDetailsWithoutColumnsObjArray(), false);
								}

							}
						}
					}
				}
			}
		} catch (Exception e) {

			log.error("May be table not performed, we are starting complete migration for it." + tableName, e);
			processCompleteMigration(tableMetaDataSource, TableStatus.TABLE_NOT_CREATED);
		}
	}

	private void createTableFromSync(TableMetaData tableMetaDataSource, TableStatus tableStatus) {
		try {
			List<SQLColumn> columns = as400Dao.getTableDesc(tableMetaDataSource, false);
			if (Objects.nonNull(columns)) {
				tableMetaDataSource.setColumns(columns);
				tableMetaDataSource.setPostgresQueries(utility.getPostgresQueries(tableMetaDataSource));
				postgresDao.createTable(tableMetaDataSource);

				if (tableMetaDataSource.getTotalRows() > 0) {
					tableMetaDataSource.getTableProcess().setStatus(TableStatus.MIGRATION_PROCESS_IN_RUNNING);
				} else {
					tableMetaDataSource.getTableProcess().setStatus(TableStatus.TABLE_CREATED_WITH_NO_DATA);
				}
				// status and other details ----------
				postgresDao.updateTableDeatil(
						tableMetaDataSource.getTableProcess().getTableDetailsWithColumnsObjArray(), true);
				postgresDao.saveIntoTableProcessDetail(new TableProcessDetail(tableMetaDataSource.getTableName(),
						"Table creation from sync" + " privious table status was :- " + tableStatus.toString())
								.getSaveObjArray());
			}
		} catch (Exception e) {
			log.error("Exception at createTable while sync !!! ", e);
			TableProcess tableProcess = new TableProcess(tableMetaDataSource.getTableName());
			tableProcess.setTotalRows(tableMetaDataSource.getTotalRows());
			tableProcess.setStatus(TableStatus.TABLE_CREATION_FAILED);
			postgresDao.updateTableProcessStatus(tableProcess.getUpdateObjArray());
			postgresDao.saveIntoTableProcessDetail(
					new TableProcessDetail(tableMetaDataSource.getTableName(), "Table creation fail at Sync "
							+ AuditMessage.TABLE_CREATION_FAILED_MSG + AuditMessage.EXECPTION_MSG + e)
									.getSaveObjArray());
		}
	}

	public Boolean processFailedBatches(List<BatchDetail> tableFailedBatchList) {
		Boolean allBatchProcess = true;
		try {
			for (BatchDetail batch : tableFailedBatchList) {
				int curAttempt = 0;
				try {
					curAttempt = postgresDao.getFailedBatchAttempt(batch);
				} catch (Exception e) {
					throw e;
				}
				if (maxAttempt > curAttempt) {
					if (Objects.nonNull(batch) && Objects.nonNull(batch.getColumnJson())) {
						List<SQLColumn> columns = null;
						try {
							columns = new ObjectMapper().readValue(batch.getColumnJson(),
									new TypeReference<List<SQLColumn>>() {
									});
						} catch (IOException e) {
							log.error(AuditMessage.EXECPTION_MSG + " Columns prasing performed ", e);
							throw e;
						}
						TableMetaData tableMetaData = new TableMetaData(batch.getTableName(), batch.getStartingRrn(),
								batch.getEndingRrn(), columns, new FailedBatchDetails(batch.getBno()));
						List<Object[]> tableData = as400Dao.readOprationOnFailedBatch(tableMetaData);
						if (Objects.nonNull(tableData)) {
							boolean dataInsert = postgresDao.writeOpraionFailedBatch(tableMetaData, tableData);
							if (dataInsert)
								postgresDao.updateBatchDetailStatus(
										new BatchDetail(batch.getBno(), BatchDetailStatus.REFACTORED)
												.getUpdateStatusObjArry());
							allBatchProcess = allBatchProcess && dataInsert;
						}
					}
				} else {
					postgresDao.updateBatchDetailStatus(
							new BatchDetail(batch.getBno(), BatchDetailStatus.MAX_ATTEMPTS_REACHED)
									.getUpdateStatusObjArry());
					allBatchProcess=false;
				}
			}
		} catch (Exception e) {
			log.error(AuditMessage.EXECPTION_MSG + " processFailedBatches :- ", e);
		}
		return allBatchProcess;
	}

}
