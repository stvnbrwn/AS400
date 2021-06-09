package com.as400datamigration.reposistory.impl;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import com.as400datamigration.common.Utility;
import com.as400datamigration.model.SQLColumn;
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
	As400Dao as400Dao;

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

		String tableDescQuery = Utility.fetchTableDesc(tableName);
		List<SQLColumn> columns = new ArrayList<>();
		try {
			log.info("Get table desc start !!!");
			columns = as400Template.query(tableDescQuery,
					(rs, num) -> new SQLColumn(rs.getString("NAME"), rs.getString("DATA_TYPE"), rs.getInt("LENGTH"),
							rs.getInt("SCALE"), rs.getString("COLUMN_HEADING")));

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
			String sqlData = Utility.getSelectQueryFor5Records(tableName);
			tableDataList = as400Template.query(sqlData, new TableResultSetExtractor(columns));
		} catch (Exception e) {
			log.error("Exception at fetchFirst5RecordsFromTable !!!");
		} 
		
		return tableDataList;
	}
	
	// 1) full insertion -> get as400 data from tables
	public List<Object[]> performOprationOnTable(String tableName, List<SQLColumn> columns) {
		log.info("Start performOprationOnTable for table : " + tableName + "Time : "+ LocalDateTime.now());
		String sqlData = Utility.getSelectQuery(tableName);
		List<Object[]> tableDataList = new ArrayList<>();
		try {
			if (!columns.isEmpty()) {
				tableDataList = as400Template.query(sqlData, new TableResultSetExtractor(columns));
			}
			log.info("Ending of performOprationOnTable !!!");
			log.info("Total data in Table : "+ tableDataList.size() + " Time : "+LocalDateTime.now());
		} catch (Exception e) {
			log.error("Exception at performOprationOnTable !!!");
		}
		return tableDataList;
	}

	public void performOprationOnTable(String tableName, long totalRecords) {

		log.info("Start performOprationOnTable for table : " + tableName);
		String sqlData = Utility.getSelectQuery(tableName);

		List<SQLColumn> columns = new ArrayList<>();
		List<Object[]> tableDataList = new ArrayList<>();

		as400Template.query(sqlData, new ResultSetExtractor<Object>() {

			@Override
			public Integer extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetMetaData rsmd = rs.getMetaData();

				int columnCount = rsmd.getColumnCount();
				for (int i = 1; i <= columnCount; i++) {
					SQLColumn column = new SQLColumn();
					column.setName(rsmd.getColumnName(i));
					column.setColumnType(rsmd.getColumnTypeName(i));
					column.setColumnSize(rsmd.getColumnDisplaySize(i));

					// json-> mapping

					// System.out.println(rsmd.get); columns.add(column);
				}

				if (!columns.isEmpty()) {

					String crtQuery = utility.getCreateQuery(tableName, columns);
					log.info("create Query : " + crtQuery);
					postgresDao.createTable(crtQuery);
					//As400Dao.getAllData(rs, columns, totalRecords, tableDataList);

					if (!tableDataList.isEmpty()) {
						String insertQuery = utility.getInsertQuery(tableName, columns);
						log.info("Insert Query : " + insertQuery);
						postgresDao.insertBatchInTable(insertQuery, tableDataList);
					}

				}
				return columnCount;

			}

		});

		// return tableDataList;

	}

		
		
	

	protected static void getAllData(ResultSet rs, List<SQLColumn> columns, long totalRecords,
			List<Object[]> tableDataList) {
		// TODO Auto-generated method stub
		
	}

}
