package com.as400datamigration.reposistory.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.as400datamigration.audit.AuditMessage;
import com.as400datamigration.audit.BatchDetailStatus;
import com.as400datamigration.audit.FailBatchStatus;
import com.as400datamigration.audit.TableStatus;
import com.as400datamigration.common.Utility;
import com.as400datamigration.model.SQLColumn;
import com.as400datamigration.model.TableMetaData;
import com.as400datamigration.model.TableProcess;
import com.as400datamigration.model.TableProcessDetail;
import com.as400datamigration.reposistory.As400Dao;
import com.as400datamigration.reposistory.PostgresDao;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class As400DaoImpl implements As400Dao {

	@Autowired
	@Qualifier("As400JdbcTemplate")
	private JdbcTemplate as400Template;

	@Autowired
	Utility utility;

	@Autowired
	PostgresDao postgresDao;

	// 1) Full insertion 4)TEST
	public long gettotalRecords(String tableName) {
		long totalRecords = 0;
		try {
			log.info("Get Total records for table : " + tableName + " time    : " + LocalDateTime.now());
			totalRecords = as400Template.queryForObject(utility.getRowCount(tableName), Long.class);
			// System.out.println("totalrecords : " + totalRecords);
		} catch (Exception e) {
			log.error("Exception at gettotalRecords !!!");
		}
		return totalRecords;
	}

	// 4)TEST
	public List<SQLColumn> getTableDesc(String tableName, boolean atCreation) {
		List<SQLColumn> columns = null;
		try {
			columns = as400Template.query(utility.fetchTableDesc(tableName),
					(rs, num) -> new SQLColumn(rs.getString("NAME"), rs.getString("DATA_TYPE"), rs.getInt("LENGTH"),
							rs.getInt("SCALE"), rs.getString("COLUMN_HEADING")));
			
			columns.add(0, new SQLColumn("RRN", "DECIMAL", 17, 0, "RRN Number"));
			
		} catch (Exception e) {
			log.error("Exception at getTableDesc !!!", e);
			if (atCreation) {
				TableProcess tableProcess = new TableProcess(tableName,TableStatus.TABLE_DESC_NOT_FOUND_AT_SOURCE,
						AuditMessage.TABLE_DESC_NOT_FOUND_AT_SOURCE_MSG + AuditMessage.EXECPTION_MSG + e);
				postgresDao.saveIntoTableProcess(tableProcess.getSaveObjArray());
			}
			else
				throw e;
		}
		return columns;
	}
	
	@Override
	public List<SQLColumn> getTableDesc(TableMetaData tableMetaData,boolean isCreate) {
		List<SQLColumn> columns = null;
		try {
			columns = as400Template.query(utility.fetchTableDesc(tableMetaData.getTableName()),
					(rs, num) -> new SQLColumn(rs.getString("NAME"), rs.getString("DATA_TYPE"), rs.getInt("LENGTH"),
							rs.getInt("SCALE"), rs.getString("COLUMN_HEADING")));
			
			columns.add(0, new SQLColumn("RRN", "DECIMAL", 17, 0, "RRN Number"));
			
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				tableMetaData.getTableProcess().setColumnsJson(gson.toJson(columns));
			
		} catch (Exception e) {
			log.error("Exception at getTableDesc !!!");
			if(isCreate) {
				postgresDao.saveIntoTableProcess(new TableProcess(tableMetaData.getTableName(),TableStatus.TABLE_DESC_NOT_FOUND_AT_SOURCE).getSaveObjArray());
				postgresDao.saveIntoTableProcessDetail(new TableProcessDetail(tableMetaData.getTableName(), 
						AuditMessage.TABLE_DESC_NOT_FOUND_AT_SOURCE_MSG + AuditMessage.EXECPTION_MSG + e).getSaveObjArray());
			}
			else {
				postgresDao.updateTableProcessStatus(new TableProcess(tableMetaData.getTableName(),TableStatus.TABLE_DESC_NOT_FOUND_AT_SOURCE).getUpdateObjArray());
				postgresDao.saveIntoTableProcessDetail(new TableProcessDetail(tableMetaData.getTableName(),
						AuditMessage.SYNC_FAIL_AT_SOURCE_MSG +
						AuditMessage.TABLE_DESC_NOT_FOUND_AT_SOURCE_MSG + AuditMessage.EXECPTION_MSG + e).getSaveObjArray());
			}	
				//change
		}
		return columns;
	}

	// 4)TEST
	public List<Object[]> fetchFirst5RecordsFromTable(String tableName, List<SQLColumn> columns) {
		List<Object[]> tableDataList = null;
		try {
			log.info("Start fetchTable5Records for table : " + tableName);
			String sqlData = utility.getSelectQueryFor5Records(tableName);
			tableDataList = as400Template.query(sqlData, new TableResultSetExtractor(columns));
		} catch (Exception e) {
			log.error("Exception at fetchFirst5RecordsFromTable !!!");
		}

		return tableDataList;
	}

	/*
	 * public List<Object[]> performReadOprationOnTable(String tableName, long
	 * offset, long totalRecords, List<SQLColumn> columns) {
	 * 
	 * log.info("Start performOprationOnTable for table : " + tableName); String
	 * sqlData; sqlData = utility.getSelectQueryForBatch(tableName, offset,
	 * totalRecords); List<Object[]> tableDataList = as400Template.query(sqlData,
	 * new TableResultSetExtractor(columns)); System.out.println(); return
	 * tableDataList; }
	 */

	@Override
	public List<Object[]> readOprationOnTable(TableMetaData tableMetaData) {
		List<Object[]> tableDataList = null;
		long bno = 0;  // doubt
		try {
			log.info("Start readOprationOnTable for table : " + tableMetaData.getTableName());
			
			//bno=postgresDao.saveBatchDetail_t(tableMetaData.getBatchDetail());
			
			bno=postgresDao.saveBatchDetail(tableMetaData.getBatchDetail().getSaveObjArray());
			//System.out.println();
			String sqlData = utility.getSelectQueryForBatch(tableMetaData.getTableName(), tableMetaData.getMinRrn(),
					tableMetaData.getMaxRrn());
			tableDataList = as400Template.query(sqlData, new TableResultSetExtractor(tableMetaData.getColumns()));

			tableMetaData.getBatchDetail().setStatus(BatchDetailStatus.ENDED_AT_SOURCE);
			tableMetaData.getBatchDetail().setEndedAtSource(LocalDateTime.now());
			tableMetaData.getBatchDetail().setModifiedAt(LocalDateTime.now());
			tableMetaData.getBatchDetail().setBno(bno);
			postgresDao.updateBatchDetail(tableMetaData.getBatchDetail().getUpdateObjArray()); // pending
		} 
		catch (Exception e) {
			
			if(bno==0)
				throw e;
			
			tableMetaData.getBatchDetail().setStatus(BatchDetailStatus.FAILED_AT_SOURCE);
			tableMetaData.getBatchDetail().setEndedAtSource(LocalDateTime.now());
			tableMetaData.getBatchDetail().setModifiedAt(LocalDateTime.now());
			tableMetaData.getBatchDetail().setColumnsJson(tableMetaData.getTableProcess().getColumnsJson());
			tableMetaData.getBatchDetail().setReason(AuditMessage.EXECPTION_MSG + e);
			       // if(bno!=0)// 0== throw
			tableMetaData.getBatchDetail().setBno(bno); 
			// zero in case of bno not found -> doubt we can not update on zero
			postgresDao.updateBatchDetail(tableMetaData.getBatchDetail().getUpdateObjArray()); // pending
			
			// pending
			/*
			 * postgresDao.updateTableProcessStatus( new
			 * TableProcess(tableMetaData.getTableName(),
			 * TableStatus.Table_Created_With_FailedBatch) .getUpdateObjArray());
			 */

		}
		log.info("ENd readOprationOnTable for table : " + tableMetaData.getTableName());
		return tableDataList;
	}
	
	@Override
	public List<Object[]> readOprationOnFailedBatch(TableMetaData tableMetaData) {
		List<Object[]> tableDataList = null;
		long fbno = 0;
		try {
			log.info("Start readOprationOnFailedBatch for table : " + tableMetaData.getTableName());
			fbno=postgresDao.saveFailedBatchDetail(tableMetaData.getFailedBatchDetails().getSaveObjArray());
			String sqlData = utility.getSelectQueryForBatch(tableMetaData.getTableName(), tableMetaData.getMinRrn(),
					tableMetaData.getMaxRrn());
			tableDataList = as400Template.query(sqlData, new TableResultSetExtractor(tableMetaData.getColumns()));
			tableMetaData.getFailedBatchDetails().setBno(fbno);
			
		} catch (Exception e) {
			tableMetaData.getFailedBatchDetails().setStatus(FailBatchStatus.FAIL);
			tableMetaData.getFailedBatchDetails().setEndedAt(LocalDateTime.now());
			tableMetaData.getFailedBatchDetails().setReason(AuditMessage.EXECPTION_MSG + e);
			if (fbno!=0) {  //doubt
				tableMetaData.getFailedBatchDetails().setBno(fbno);
			}
			postgresDao.updateFailedBatchDetail(tableMetaData.getFailedBatchDetails().getUpdateObjArray()); // pending
		}
		return tableDataList;
	}

	// done
	public TableMetaData getTableMetaData(String tableName,boolean fromTableCreate) {
		TableMetaData tableMetaData = null;
		TableProcess tableProcess = new TableProcess(tableName);
		try {
			//postgresDao.saveIntoTableProcess(tableProcess.getSaveObjArray());
			tableMetaData = (TableMetaData) as400Template.queryForObject(utility.getTableMetaDataSource(tableName),
					new BeanPropertyRowMapper<TableMetaData>(TableMetaData.class));
			
			tableProcess.setTotalRows(tableMetaData.getTotalRows());
			tableProcess.setMinRrn(tableMetaData.getMinRrn());
			tableProcess.setMaxRrn(tableMetaData.getMaxRrn());
			tableMetaData.setTableProcess(tableProcess);
		} catch (Exception e) {
			log.info("Table not found at source..",e);
			if (fromTableCreate) {
				postgresDao.saveIntoTableProcess(new TableProcess(tableName,TableStatus.TABLE_NOT_FOUND_AT_SOURCE).getSaveObjArray());
				postgresDao.saveIntoTableProcessDetail(new TableProcessDetail(tableName, 
						AuditMessage.TABLE_NOT_FOUND_AT_SOURCE_MSG + AuditMessage.EXECPTION_MSG + e).getSaveObjArray());
			}else {
				postgresDao.updateTableProcessStatus(new TableProcess(tableMetaData.getTableName(),
						TableStatus.TABLE_NOT_FOUND_AT_SOURCE).getUpdateObjArray());
				postgresDao.saveIntoTableProcessDetail(new TableProcessDetail(tableName,
						AuditMessage.SYNC_FAIL_AT_SOURCE_MSG + 
						AuditMessage.TABLE_NOT_FOUND_AT_SOURCE_MSG + AuditMessage.EXECPTION_MSG + e).getSaveObjArray());
				
				//throw e;
			}
			
		}
		return tableMetaData;
	}

	

	

	/*
	 * @Override public List<Object[]> performOprationOnTable(TableMetaData
	 * tableMetaData, List<SQLColumn> columns) { // TODO Auto-generated method stub
	 * return null; }
	 */

}
