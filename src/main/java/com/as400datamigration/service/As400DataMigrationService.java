package com.as400datamigration.service;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.as400datamigration.audit.AuditMessage;
import com.as400datamigration.audit.TableStatus;
import com.as400datamigration.common.LogMessage;
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

	@Autowired
	Utility utility;

	@Autowired
	As400Dao as400Dao;

	@Autowired
	PostgresDao postgresDao;

	@Async("ThreadExecutor")
	public void processCompleteMigration(TableMetaData tableMetaData, TableStatus tableStatus) {
		try {
			if (tableStatus.equals(TableStatus.TABLE_NOT_CREATED))
				tableMetaData = createTable(tableMetaData.getTableName());
			if (tableStatus.equals(TableStatus.TABLE_NOT_FOUND_AT_SOURCE)
					|| tableStatus.equals(TableStatus.TABLE_DESC_NOT_FOUND_AT_SOURCE)
					|| tableStatus.equals(TableStatus.TABLE_CREATION_FAILED))
				createTableFromSync(tableMetaData, tableStatus);

			if (Objects.nonNull(tableMetaData.getColumns())) {
				boolean allBatchInsert = performReadWriteOnTable(tableMetaData);
				if (allBatchInsert) {
					postgresDao.updateTableProcessStatus(new TableProcess(tableMetaData.getTableName(),
							TableStatus.TABLE_CREATED_AND_ALL_BATCH_COMPLETED).getUpdateObjArray());
				}
			}
		} catch (DuplicateKeyException e) {
			System.out.println(
					LogMessage.ALIEN_CENTER + tableMetaData.getTableName() + " Table migration is already performed.");
			System.out.println(LogMessage.ALIEN_CENTER
					+ "Please select option 2 from main menu for syncing the additional data for this table.");
		} catch (Exception e) {
			System.out.println(LogMessage.ALIEN_CENTER + "Please check connection or " + tableMetaData.getTableName()
					+ " already performed");
			log.error(AuditMessage.EXECPTION_MSG + " May be columns meta data not found \n"
					+ "Error at processCompleteMigration ", e);
			//
		}
	}

	private boolean performReadWriteOnTable(TableMetaData tableMetaData) {
		boolean allBatchInsert = true;
		log.info("start performReadWriteOnTable execution ..!");
		if (tableMetaData.getTotalRows() > 0) {
			long maxRrn = tableMetaData.getMaxRrn();
			long totalBatch = maxRrn / batchSize;
			if (maxRrn % batchSize > 0)
				totalBatch++;
			for (int i = 0; i < totalBatch - 1; i++) {
				tableMetaData.setMaxRrn(tableMetaData.getMinRrn() + batchSize - 1);
				allBatchInsert = batchOpration(tableMetaData, allBatchInsert);
				tableMetaData.setMinRrn(tableMetaData.getMinRrn() + batchSize);
			}
			allBatchInsert = batchOpration(tableMetaData, allBatchInsert);

			/*
			 * while (tableMetaData.getMinRrn() < maxRrn) {
			 * tableMetaData.setMaxRrn(tableMetaData.getMinRrn() + batchSize - 1);
			 * allBatchInsert = batchOpration(tableMetaData, allBatchInsert);
			 * tableMetaData.setMinRrn(tableMetaData.getMinRrn() + batchSize); }
			 * if(tableMetaData.getMaxRrn()<maxRrn) { tableMetaData.setMaxRrn(maxRrn);
			 * allBatchInsert = batchOpration(tableMetaData, allBatchInsert); }
			 */
		}
		// System.out.println("stop !!!");
		log.info("End performReadWriteOnTable execution ..!");
		return allBatchInsert;
	}

	private boolean batchOpration(TableMetaData tableMetaData, boolean allBatchInsert) {
		log.info("start batchOpration execution..! ");
		List<Object[]> tableData = null;
		tableMetaData.setBatchDetail(new BatchDetail(tableMetaData));
		tableData = as400Dao.readOprationOnTable(tableMetaData);
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
		log.info("start createTable execution ...! ");
		TableMetaData tableMetaData = null;
		// TableProcess tableProcess = new TableProcess(tableName);
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
						tableMetaData.getTableProcess().setStatus(TableStatus.TABLE_CREATED_AND_IN_RUNNING);
					} else {
						tableMetaData.getTableProcess().setStatus(TableStatus.TABLE_CREATED_WITH_NO_DATA);
					}

					// tableMetaData.getTableProcess().setCreateAt(LocalDateTime.now());
					// tableMetaData.setTableProcess(tableProcess);
					postgresDao.saveIntoTableProcess(tableMetaData.getTableProcess().getSaveObjArray());
				}
			}

		} catch (Exception e) {
			log.info("Exception At createTable !!! ");
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
					if (tableProcessDestination.getStatus().equals(TableStatus.TABLE_NOT_FOUND_AT_SOURCE)
							|| tableProcessDestination.getStatus().equals(TableStatus.TABLE_DESC_NOT_FOUND_AT_SOURCE)
							|| tableProcessDestination.getStatus().equals(TableStatus.TABLE_CREATION_FAILED)) {
						processCompleteMigration(tableMetaDataSource, tableProcessDestination.getStatus());
					} else {
						BatchDetail lastBatchDetail = postgresDao.getlastBatchDetails(tableName);
						if (Objects.nonNull(lastBatchDetail) && Objects.nonNull(tableProcessDestination.getColumnJson())
								&& !tableProcessDestination.getColumnJson().isEmpty()) {
							if (tableMetaDataSource.getTableProcess().getMaxRrn() > tableProcessDestination
									.getMaxRrn()) {
								List<SQLColumn> columns = new ObjectMapper().readValue(
										tableProcessDestination.getColumnsJson(), new TypeReference<List<SQLColumn>>() {
										});
								boolean isSynced = performReadWriteOnTable(new TableMetaData(
										tableMetaDataSource.getTotalRows(), tableProcessDestination.getMaxRrn(), // minRrn
																													// from
																													// destination
										tableMetaDataSource.getMaxRrn()));
								if (isSynced) {
									postgresDao.updateTableProcessStatus(
											new TableProcess(tableName, TableStatus.TABLE_SYNC_SECCUSSFUL)
													.getUpdateObjArray());
								} else {
									postgresDao.updateTableProcessStatus(
											new TableProcess(tableName, TableStatus.TABLE_SYNC_FAIL)
													.getUpdateObjArray());
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {

			log.info("May be table not performed, we are starting complete migration for it.", e);
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
					tableMetaDataSource.getTableProcess().setStatus(TableStatus.TABLE_CREATED_AND_IN_RUNNING);
				} else {
					tableMetaDataSource.getTableProcess().setStatus(TableStatus.TABLE_CREATED_WITH_NO_DATA);
				}
				// status and other details----------
				postgresDao.updateTableDeatil(tableMetaDataSource.getTableProcess().getTableDetailsObjArray());
				postgresDao.saveIntoTableProcessDetail(new TableProcessDetail(tableMetaDataSource.getTableName(),
						"Table Creation From Sync" + "Privious table Status was :- " + tableStatus.toString())
								.getSaveObjArray());
			}
		} catch (Exception e) {
			log.info("Exception At createTable while Sync !!! ");
			TableProcess tableProcess = new TableProcess(tableMetaDataSource.getTableName());
			tableProcess.setTotalRows(tableMetaDataSource.getTotalRows());
			tableProcess.setStatus(TableStatus.TABLE_CREATION_FAILED);
			postgresDao.updateTableProcessStatus(tableProcess.getUpdateObjArray());
			postgresDao.saveIntoTableProcessDetail(
					new TableProcessDetail(tableMetaDataSource.getTableName(), "Table Creation Fail at Sync "
							+ AuditMessage.TABLE_CREATION_FAILED_MSG + AuditMessage.EXECPTION_MSG + e)
									.getSaveObjArray());
		}
	}

	public void processFailedBatches(BatchDetail batch) {
		List<Object[]> tableData = null;

		List<SQLColumn> columns = null;
		try {
			columns = new ObjectMapper().readValue(batch.getColumnsJson(), new TypeReference<List<SQLColumn>>() {
			});
			TableMetaData tableMetaData = new TableMetaData(batch.getTableName(), batch.getStartingRrn(),
					batch.getEndingRrn(), columns, new FailedBatchDetails(batch.getBno()));

			tableData = as400Dao.readOprationOnFailedBatch(tableMetaData);
			if (Objects.nonNull(tableData)) {
				postgresDao.writeOpraionFailedBatch(tableMetaData, tableData);
			}
		} catch (IOException e) {
			// doubt
			log.error("columns parsing :-", e);
		}

	}

}
