package com.as400datamigration.reposistory.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.as400datamigration.common.AuditMessage;
import com.as400datamigration.common.Utility;
import com.as400datamigration.model.SQLColumn;
import com.as400datamigration.model.TableMetaData;
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
			//System.out.println("totalrecords : " + totalRecords);
		} catch (Exception e) {
			log.error("Exception at gettotalRecords !!!");
		}
		return totalRecords;
	}

	// 1) Full insertion 4)TEST
	public List<SQLColumn> getTableDesc(String tableName) {

		String tableDescQuery = utility.fetchTableDesc(tableName);
		List<SQLColumn> columns = new ArrayList<>();
		try {
			log.info("Get table desc start !!!");
			columns = as400Template.query(tableDescQuery,
					(rs, num) -> new SQLColumn(rs.getString("NAME"), rs.getString("DATA_TYPE"), rs.getInt("LENGTH"),
							rs.getInt("SCALE"), rs.getString("COLUMN_HEADING")));
			
			//name=RRN, columnType=DECIMAL, columnSize=17
			columns.add(0,new SQLColumn("RRN","DECIMAL",17,0,"RRN Number"));
			
//			utility.setPostgresDataType(columns);
		} catch (Exception e) {
			log.error("Exception at getTableDesc !!!");
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
	
	// 1) full insertion -> get as400 data from tables
	/*
	 * public List<Object[]> performOprationOnTable(String tableName,
	 * List<SQLColumn> columns) {
	 * log.info("Start performOprationOnTable for table : " + tableName + "Time : "+
	 * LocalDateTime.now()); String sqlData = utility.getSelectQuery(tableName);
	 * List<Object[]> tableDataList = new ArrayList<>(); try { if
	 * (!columns.isEmpty()) { tableDataList = as400Template.query(sqlData, new
	 * TableResultSetExtractor(columns)); }
	 * log.info("Ending of performOprationOnTable !!!");
	 * log.info("Total data in Table : "+ tableDataList.size() +
	 * " Time : "+LocalDateTime.now()); } catch (Exception e) {
	 * log.error("Exception at performOprationOnTable !!!"); } return tableDataList;
	 * }
	 */

	public List<Object[]> performOprationOnTable(String tableName,long offset, long totalRecords,List<SQLColumn> columns) {

		log.info("Start performOprationOnTable for table : " + tableName);
		String sqlData;
		// improve
		/*
		 * if(offset==0) sqlData= utility.getSelectQuery(tableName); else
		 */
		try {
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		sqlData = utility.getSelectQueryForBatch(tableName, offset, totalRecords);
		List<Object[]> tableDataList = as400Template.query(sqlData, new TableResultSetExtractor(columns));
		System.out.println();
		return tableDataList;

	}

	
	/*
	 * @Override public List<Object[]> performOprationOnTable(String tableName, long
	 * totalRecords) { log.info("Start performOprationOnTable for table : " +
	 * tableName); String sqlData = utility.getSelectQuery(tableName);
	 * 
	 * List<Object[]> tableDataList = as400Template.query(sqlData, new
	 * TableResultSetExtractor()); return tableDataList; }
	 */
	 

	
	
	public TableMetaData getTableMetaData(String tableName) {
		TableMetaData tableMetaData = new TableMetaData();
		try {
			log.info("Get Total records for table : " + tableName + " time    : " + LocalDateTime.now());
			tableMetaData = (TableMetaData) as400Template.queryForObject(utility.getTableMetaData(tableName), new BeanPropertyRowMapper<TableMetaData>(TableMetaData.class));
			//System.out.println("totalrecords : " + totalRecords);
		} catch (Exception e) {
			log.error("Exception at getTableMetaData !!!" , e);
			// table_name VARCHAR, total_rows NUMERIC, status VARCHAR, reason VARCHAR
			postgresDao.saveIntoAllTableProcess(new Object[] {tableName,tableMetaData.getTotalRows(),AuditMessage.TABLE_STATE_FAILED,
					AuditMessage.TABLE_STATE_FAILED_MESSAGE_AT_GETMETADATA +  e});
		}
		return tableMetaData;
	}

	/*
	 * @Override public List<Object[]> performOprationOnTable(TableMetaData
	 * tableMetaData, List<SQLColumn> columns) { // TODO Auto-generated method stub
	 * return null; }
	 */

}
