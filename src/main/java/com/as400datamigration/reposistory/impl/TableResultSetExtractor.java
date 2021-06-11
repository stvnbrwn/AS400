package com.as400datamigration.reposistory.impl;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.as400datamigration.model.SQLColumn;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TableResultSetExtractor implements ResultSetExtractor<List<Object[]>>{
	
	List<SQLColumn> columns=new ArrayList<SQLColumn>();
	//int totalRecords;
	
	@Override
	public List<Object[]> extractData(ResultSet rs) throws SQLException, DataAccessException {
			
		/*
		 * ResultSetMetaData rsmd = rs.getMetaData();
		 * 
		 * int columnCount = rsmd.getColumnCount();
		 */
		/*
		 * if ( columns.isEmpty()) {
		 * 
		 * for (int i = 1; i <= columnCount; i++) { SQLColumn column = new SQLColumn();
		 * column.setName(rsmd.getColumnName(i));
		 * column.setColumnType(rsmd.getColumnTypeName(i));
		 * column.setColumnSize(rsmd.getColumnDisplaySize(i));
		 * column.setScale(rsmd.getPrecision(i)); // json-> mapping
		 * 
		 * // System.out.println(rsmd.get); columns.add(column); } }
		 */
		
		List<Object[]> tableDataList=new ArrayList<>();
		log.info("Get all data form table starts , Total Columns " + columns.size() +
				 "Time : " + LocalDateTime.now());
		try {
			while (rs.next()) {
				List<Object> objList = new ArrayList<>();
				for (int j = 0; j < columns.size(); j++) {
					objList.add(getColumnValue(rs, columns.get(j).getColumnType(),columns.get(j).getName(),
							columns.get(j).getScale()));
				}
				// SQLColumn.setColumnvalues();
				tableDataList.add(objList.toArray());
			}
			log.info("Get all data form table ENDS   , Total Columns " + columns.size() +
					"Total  fetch  records : "+ tableDataList.size() + "Time : " + LocalDateTime.now());
		} catch (Exception e) {
			log.error("getAllData from table fails !!!");
			e.printStackTrace();
		}
		return tableDataList;
	}

	/*
	 * public TableResultSetExtractor(List<SQLColumn> columns,int totalRecords) {
	 * super(); this.columns=columns; this.totalRecords=totalRecords;
	 * 
	 * }
	 */
	
	public TableResultSetExtractor(List<SQLColumn> columns) {
		super();
		this.columns=columns;
	}

	public TableResultSetExtractor() {
	}

	private static Object getColumnValue(ResultSet rs,String columnType, String ColumnName, int scale) {
		
		try {
			switch (columnType) {

			case "CHAR":
				return rs.getString(ColumnName);
			
			case "DECIMAL": // P -> decimal  // scale > 0 // vikas sir scale=6 or 2 // only 4 fields
			case "NUMERIC": // S -> decimal  // scale > 0 // vikas sir scale=6 or 2 // only 4 fields
				if(scale>0) 
					return rs.getBigDecimal(ColumnName);
				return rs.getLong(ColumnName);
		
			case "INTEGER":  //B //only zero 4 digit
				return rs.getInt(ColumnName);

			/*
			 * case "DECIMAL": // P -> decimal // scale > 0 // vikas sir scale=6 or 2 //
			 * only 4 fields return rs.getBigDecimal(ColumnName);
			 */

			default:
				throw new Exception(columnType);

			}
		} catch (Exception e) {
			log.error("column Type not available mismatch :" + e.getMessage());
			e.printStackTrace();
		}

		return null;
	}


}
