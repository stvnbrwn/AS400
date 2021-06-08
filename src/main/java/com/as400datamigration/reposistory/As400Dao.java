package com.as400datamigration.reposistory;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import com.as400datamigration.common.Constant;
import com.as400datamigration.common.Utility;
import com.as400datamigration.model.SQLColumn;
import com.ibm.as400.access.ReturnCodeException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class As400Dao {

	@Autowired
	@Qualifier("As400JdbcTemplate")
	private JdbcTemplate as400Template;

	@Autowired
	Utility utility;

	@Autowired
	As400Dao as400Dao;

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
					column.setColumnSize(rsmd.getColumnDisplaySize(i));

					// json-> mapping

					// System.out.println(rsmd.get);
					columns.add(column);
				}

				if (!columns.isEmpty()) {

					String crtQuery = utility.getCreateQuery(tableName, columns);
					log.info("create Query : " + crtQuery);
					postgresDao.createTable(crtQuery);
					As400Dao.getAllData(rs, columns, totalRecords, tableDataList);

					if (!tableDataList.isEmpty()) {
						String insertQuery = utility.getInsertQuery(tableName, columns);
						log.info("Insert Query : " + insertQuery);
						postgresDao.insertBatchInTable(insertQuery, tableDataList);
					}

				}
				return columnCount;
			}

		});

		return tableDataList;
	}

	protected static void getAllData(ResultSet rs, List<SQLColumn> columns, long totalRecords,
			List<Object[]> tableDataList) {
		log.info("Get all data form table starts : \n Total Columns " + columns.size() + "Total records in table : "
				+ totalRecords + "Time : " + LocalDateTime.now());
		try {
			while (rs.next()) {
				List<Object> objList = new ArrayList<>();
				for (int j = 0; j < columns.size(); j++) {
					objList.add(getColumnValue(rs, columns, j));
				}
				// SQLColumn.setColumnvalues();
				tableDataList.add(objList.toArray());
			}
			log.info("Get all data form table ENDS   : \n Total Columns " + columns.size() + "Total  fetch  records : "
					+ tableDataList.size() + "Time : " + LocalDateTime.now());
		} catch (Exception e) {
			log.error("getAllData from table fails !!!");
			e.printStackTrace();
		}

	}
	
	private static Object getColumnValue(ResultSet rs, List<SQLColumn> columns, int j) {
		String columnType = columns.get(j).getColumnType(), colName = columns.get(j).getName();
		try {
			switch (columnType) {

			case "CHAR":
				// columns.get(j).setColumnType("character varying");
				return rs.getString(colName);

			case "CHAR () FOR BIT DATA":
				// columns.get(j).setColumnType("character varying");
				System.out.println("char bit");
				System.out.println(columns.get(j).getColumnSize());
				// String s=Base64.getEncoder().encodeToString(rs.getBytes(j));
				return rs.getString(colName);

			case "NUMERIC": // decimal
				System.out.println("numeric");
				System.out.println(columns.get(j).getColumnSize());
				return rs.getLong(colName);
			// scale > 0 // vikas sir scale=6 or 2 // only 4 fields
			// then rs.getBigDecimal(colName);

			case "INTEGER":
				// B
				// LPBUFO, columnType=INTEGER,
				// columnSize=4, <-------- imp
				// scale=0,
				// ColumnHeading= Buffer Offset
				System.out.println(columns.get(j).getColumnSize());
				return rs.getLong(colName);

			case "DECIMAL":
				return rs.getBigDecimal(colName);

			default:
				System.out.println("no data type");
				// throw new Exception(columnType);

			}
		} catch (Exception e) {
			log.error("column Type not available mismatch :" + e.getMessage());
			e.printStackTrace();
		}

		return null;
	}

//.getColumnType(), columns.get(j).getName()
	
	public long gettotalRecords(String tableName) {
		long totalRecords = 0;
		try {
			log.info("Get Total records for table : " + tableName + " time    : " + LocalDateTime.now());
			totalRecords = as400Template.queryForObject(utility.getRowCount(tableName), Long.class);
			System.out.println("totalrecords : " + totalRecords);
			log.info("Total records in the table  : " + totalRecords + " time : " + LocalDateTime.now());
		} catch (Exception e) {
			// TODO: handle exception
		}
		return totalRecords;
	}

	public List<SQLColumn> getTableDesc(String tableName) {

		String tableDescQuery = Utility.fatchTableDesc(tableName);

		List<SQLColumn> columns = as400Template.query(tableDescQuery, (rs, num) -> new SQLColumn(rs.getString("NAME"),
				rs.getString("DATA_TYPE"), rs.getInt("LENGTH"), rs.getInt("SCALE"), rs.getString("COLUMN_HEADING")));
		System.out.println("col count : " + columns.size());
		columns.forEach(e -> {
			System.out.println(e.toString());
		});

		return columns;
	}

	public void fatchTable5Records(String tableName, List<SQLColumn> columns) {

		log.info("Start performOprationOnTable for table : " + tableName);
		String sqlData = Utility.getSelectQueryFor5Records(tableName);

		TableResultSetExtractor tableResultSetExtractor = new TableResultSetExtractor(columns, 5);

		List<Object[]> tableDataList = as400Template.query(sqlData, tableResultSetExtractor);

		tableDataList.forEach(e->{
			System.out.println(Arrays.toString(e));
		});
	}

}
