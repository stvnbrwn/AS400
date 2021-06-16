package com.as400datamigration.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.as400datamigration.audit.AuditMessage;
import com.as400datamigration.audit.TableStatus;
import com.as400datamigration.common.Utility;
import com.as400datamigration.model.BatchDetail;
import com.as400datamigration.model.FailedBatchDetails;
import com.as400datamigration.model.SQLColumn;
import com.as400datamigration.model.TableMetaData;
import com.as400datamigration.model.TableProcess;
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

	public void processCompleteMigration(String tableName) {
		TableMetaData tableMetaData = createTable(tableName);

		if (Objects.nonNull(tableMetaData)) {
			performReadWriteOnTable(tableMetaData);
		}
	}

	private void performReadWriteOnTable(TableMetaData tableMetaData) {
		List<Object[]> tableData = null;
		if (tableMetaData.getTotalRows() > 0) {
			long maxRrn = tableMetaData.getMaxRrn();
			// long totalrows = 0; // extra
			boolean allBatchInsert = true;
			while (tableMetaData.getMinRrn() < maxRrn) {
				tableMetaData.setMaxRrn(tableMetaData.getMinRrn() + batchSize - 1);
				if (tableMetaData.getMaxRrn() > maxRrn) {
					tableMetaData.setMaxRrn(maxRrn);
				}
				tableMetaData.setBatchDetail(new BatchDetail(tableMetaData));
				tableData = as400Dao.readOprationOnTable(tableMetaData);
				if (Objects.nonNull(tableData)) {
					boolean batchInsert = postgresDao.writeOpraionOnTable(tableMetaData, tableData);
					allBatchInsert = allBatchInsert && batchInsert;

				} else {
					allBatchInsert = false;
				}

				tableMetaData.setMinRrn(tableMetaData.getMinRrn() + batchSize);
				// totalrows += tableData.size(); // extra
			}
			if (allBatchInsert) {
				postgresDao.updateTableProcessStatus(new TableProcess(tableMetaData.getTableName(),
						TableStatus.Table_Created_And_AllBatchCompleted).getUpdateObjArray());
			}

		}
		System.out.println("stop !!!");
	}
	

	@Transactional
	private TableMetaData createTable(String tableName) {
		TableMetaData tableMetaData = null;
		// TableProcess tableProcess = new TableProcess(tableName);
		try {
			tableMetaData = as400Dao.getTableMetaData(tableName,true);
			if (Objects.nonNull(tableMetaData)) {
				tableMetaData.setTableName(tableName);
				List<SQLColumn> columns = as400Dao.getTableDesc(tableMetaData);
				if (Objects.nonNull(columns)) {
					tableMetaData.setColumns(columns);

					tableMetaData.setPostgresQueries(utility.getPostgresQueries(tableMetaData));

					postgresDao.createTable(tableMetaData);

					if (tableMetaData.getTotalRows() > 0) {
						tableMetaData.getTableProcess().setStatus(TableStatus.Table_Created_And_InRunning);
					} else {
						tableMetaData.getTableProcess().setStatus(TableStatus.Table_Created_With_NO_Data);
					}

					// tableMetaData.setTableProcess(tableProcess);
					postgresDao.saveIntoTableProcess(tableMetaData.getTableProcess().getSaveObjArray());
				}
			}

		} catch (Exception e) {
			log.info("Exception At create Table !!! ", e);
			TableProcess tableProcess = new TableProcess(tableName);
			tableProcess.setTotalRows(0l);
			tableProcess.setStatus(TableStatus.Table_Creation_Failed);
			tableProcess.setReason(AuditMessage.Table_Creation_Failed_Msg + AuditMessage.Execption_Msg + e);
			postgresDao.saveIntoTableProcess(tableProcess.getSaveObjArray());
		}
		return tableMetaData;
	}

	public void processSyncInsert(String tableName) {
		try {
			TableMetaData tableMetaDataSource = new TableMetaData(tableName);
			TableProcess tableProcessDestination = postgresDao.getTableMetaDataFromDestination(tableMetaDataSource);
			if (Objects.nonNull(tableProcessDestination)) {
				List<SQLColumn> columns = null;
				try {
					tableMetaDataSource = as400Dao.getTableMetaData(tableName,false);
					if(tableMetaDataSource.getTableProcess().getMaxRrn()>tableProcessDestination.getMaxRrn()) {
						columns = new ObjectMapper().readValue(tableProcessDestination.getColumnsJson(), new TypeReference<List<SQLColumn>>() {});
						performReadWriteOnTable(new TableMetaData(
								tableMetaDataSource.getTotalRows(),
								tableProcessDestination.getMaxRrn(), //minRrn from destination
								tableMetaDataSource.getMaxRrn()
								));
					}
				} catch (Exception e) {
					// not able to read data from as400 for sync table
				}
			}else {
				
				//check in as400 is its there --> new table which is previously 
				// not in complete migration
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			// if we throw e then no table found in tableprocess
		}
	}


	public void processFailedBatches(BatchDetail batch) {
		List<Object[]> tableData = null;

		List<SQLColumn> columns = null;
		try {
			columns = new ObjectMapper().readValue(batch.getColumnsJson(), new TypeReference<List<SQLColumn>>() {});
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
