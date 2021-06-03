package com.as400datamigration.reposistory;

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

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class As400Dao {

	@Autowired
	@Qualifier("As400JdbcTemplate")
	private JdbcTemplate as400Template;

	@Autowired
	PostgresDao postgresDao;

	public List<Object[]> performOprationOnTable(String tableName, long totalRecords) {

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
					column.setColumnSize(rsmd.getColumnDisplaySize(columnCount));
					columns.add(column);
				}

				if (!columns.isEmpty()) {
					String crtQuery = Utility.getCreateQuery(tableName, columns);
					log.info("create Query : " + crtQuery);
					postgresDao.createTable(crtQuery);
					As400Dao.getAllData(rs, columns,totalRecords ,tableDataList);
					
					if (!tableDataList.isEmpty()) {
						String insertQuery = Utility.getInsertQuery(tableName, columns);
						log.info("Insert Query : " + insertQuery);
						postgresDao.insertBatchInTable(insertQuery, tableDataList);
					}

				}
				return columnCount;
			}
		});

		return tableDataList;
	}

	protected static void getAllData(ResultSet rs, List<SQLColumn> columns, long totalRecords, List<Object[]> tableDataList) {
		log.info("Get all data form table starts : \n Total Columns " + columns.size() +
				"Total records in table : "+ totalRecords + "Time : " + LocalDateTime.now());
		try {
			while (rs.next()) {
				List<Object> objList = new ArrayList<>();
				for (int j = 0; j < columns.size(); j++) {
					objList.add(getColumnValue(rs, columns.get(j).getColumnType(), columns.get(j).getName()));
				}
				// SQLColumn.setColumnvalues();
				tableDataList.add(objList.toArray());
			}
			log.info("Get all data form table ENDS   : \n Total Columns " + columns.size() +
					"Total  fetch  records : "+ tableDataList.size() + "Time : " + LocalDateTime.now());
		} catch (Exception e) {
			log.error("getAllData from table fails !!!");
			e.printStackTrace();
		}

	}

	private static Object getColumnValue(ResultSet rs, String columnType, String colName) {
		try {
			switch (columnType) {

			case "CHAR":
				return rs.getString(colName);

			case "NUMERIC":
				return rs.getInt(colName);

			case "DECIMAL":
				return rs.getBigDecimal(colName);

			default:
				throw new Exception(columnType);

			}
		} catch (Exception e) {
			log.error("column Type not available mismatch :" + e.getMessage());
			e.printStackTrace();
		}

		return null;
	}

	public long gettotalRecords(String tableName) {
		long totalRecords = 0;
		try {
			log.info("Get Total records for table : " + tableName + " time    : " + LocalDateTime.now());
			totalRecords = as400Template.queryForObject(Utility.getRowCount(tableName), Long.class);
			log.info("Total records in the table  : " + totalRecords + " time : " + LocalDateTime.now());
		} catch (Exception e) {
			// TODO: handle exception
		}
		return totalRecords;
	}

}
