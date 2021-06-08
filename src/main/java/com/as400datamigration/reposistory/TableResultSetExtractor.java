package com.as400datamigration.reposistory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.as400datamigration.model.SQLColumn;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TableResultSetExtractor implements ResultSetExtractor<List<Object[]>>{
	
	List<SQLColumn> columns;
	int totalRecords;
	
	@Override
	public List<Object[]> extractData(ResultSet rs) throws SQLException, DataAccessException {
		
		List<Object[]> tableDataList=new ArrayList<>();
		log.info("Get all data form table starts : \n Total Columns " + columns.size() +
				"Total records in table : "+ totalRecords + "Time : " + LocalDateTime.now());
		try {
			while (rs.next()) {
				List<Object> objList = new ArrayList<>();
				for (int j = 0; j < columns.size(); j++) {
					objList.add(getColumnValue(rs, columns,j));
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
		return tableDataList;
	}

	public TableResultSetExtractor(List<SQLColumn> columns,int totalRecords) {
		super();
		this.columns=columns;
		this.totalRecords=totalRecords;

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


}
