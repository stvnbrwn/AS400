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
import com.as400datamigration.audit.TableStatus;
import com.as400datamigration.common.Utility;
import com.as400datamigration.model.SQLColumn;
import com.as400datamigration.model.TableMetaData;
import com.as400datamigration.model.TableProcess;
import com.as400datamigration.reposistory.As400Dao;
import com.as400datamigration.reposistory.PostgresDao;

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

	// 1) Full insertion 4)TEST
	public List<SQLColumn> getTableDesc(String tableName) {
		List<SQLColumn> columns = null;
		try {
			columns = as400Template.query(utility.fetchTableDesc(tableName),
					(rs, num) -> new SQLColumn(rs.getString("NAME"), rs.getString("DATA_TYPE"), rs.getInt("LENGTH"),
							rs.getInt("SCALE"), rs.getString("COLUMN_HEADING")));

			columns.add(0, new SQLColumn("RRN", "DECIMAL", 17, 0, "RRN Number"));
		} catch (Exception e) {
			log.error("Exception at getTableDesc !!!", e);
			TableProcess tableProcess = new TableProcess(tableName, 0l, TableStatus.Table_Not_Found_At_Source,
					AuditMessage.Table_Not_Found_At_Source_Msg + AuditMessage.Execption_Msg + e);
			postgresDao.saveIntoTableProcess(tableProcess.getSaveObjArray());
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

	public List<Object[]> performReadOprationOnTable(String tableName, long offset, long totalRecords,
			List<SQLColumn> columns) {

		log.info("Start performOprationOnTable for table : " + tableName);
		String sqlData;
		sqlData = utility.getSelectQueryForBatch(tableName, offset, totalRecords);
		List<Object[]> tableDataList = as400Template.query(sqlData, new TableResultSetExtractor(columns));
		System.out.println();
		return tableDataList;

	}

	@Override
	public List<Object[]> readOprationOnTable(TableMetaData tableMetaData) {
		List<Object[]> tableDataList = null;
		try {
			log.info("Start performOprationOnTable for table : " + tableMetaData.getTableName());
			postgresDao.saveBatchDetail(tableMetaData.getBatchDetail().getSaveObjArray());
			String sqlData = utility.getSelectQueryForBatch(tableMetaData.getTableName(), tableMetaData.getMinRrn(),
					tableMetaData.getMaxRrn());
			tableDataList = as400Template.query(sqlData, new TableResultSetExtractor(tableMetaData.getColumns()));

			tableMetaData.getBatchDetail().setStatus(BatchDetailStatus.Ended_At_Source);
			tableMetaData.getBatchDetail().setEndedAtSource(LocalDateTime.now());
			tableMetaData.getBatchDetail().setModifiedAt(LocalDateTime.now());
			postgresDao.updateBatchDetail(tableMetaData.getBatchDetail().getUpdateObjArray()); // pending

		} catch (Exception e) {
			tableMetaData.getBatchDetail().setStatus(BatchDetailStatus.Failed_At_Source);
			tableMetaData.getBatchDetail().setEndedAtSource(LocalDateTime.now());
			tableMetaData.getBatchDetail().setModifiedAt(LocalDateTime.now());
			tableMetaData.getBatchDetail().setReason(AuditMessage.Execption_Msg + e);
			postgresDao.updateBatchDetail(tableMetaData.getBatchDetail().getUpdateObjArray()); // pending
			// pending
			postgresDao.updateTableProcessStatus(
					new TableProcess(tableMetaData.getTableName(), TableStatus.Table_Created_With_FailedBatch)
							.getUpdateObjArray());

		}

		return tableDataList;
	}

	// done
	public TableMetaData getTableMetaData(String tableName) {
		TableMetaData tableMetaData = null;
		try {
			tableMetaData = (TableMetaData) as400Template.queryForObject(utility.getTableMetaData(tableName),
					new BeanPropertyRowMapper<TableMetaData>(TableMetaData.class));
		} catch (Exception e) {
			TableProcess tableProcess = new TableProcess(tableName, 0l, TableStatus.Table_Not_Found_At_Source,
					AuditMessage.Table_Not_Found_At_Source_Msg + AuditMessage.Execption_Msg + e);
			postgresDao.saveIntoTableProcess(tableProcess.getSaveObjArray());
		}
		return tableMetaData;
	}

	/*
	 * @Override public List<Object[]> performOprationOnTable(TableMetaData
	 * tableMetaData, List<SQLColumn> columns) { // TODO Auto-generated method stub
	 * return null; }
	 */

}
