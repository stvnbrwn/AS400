package com.as400datamigration.reposistory.impl;

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
public class TableResultSetExtractor implements ResultSetExtractor<List<Object[]>> {

	List<SQLColumn> columns = new ArrayList<SQLColumn>();

	@Override
	public List<Object[]> extractData(ResultSet rs) throws SQLException, DataAccessException {

		List<Object[]> tableDataList = new ArrayList<>();
		log.info("Get all data form table starts , Total Columns " + columns.size() + "Time : " + LocalDateTime.now());
		try {
			while (rs.next()) {
				List<Object> objList = new ArrayList<>();
				for (int j = 0; j < columns.size(); j++) {
					objList.add(getColumnValue(rs, columns.get(j).getColumnType(), columns.get(j).getName(),
							columns.get(j).getScale()));
				}
				tableDataList.add(objList.toArray());
			}
			log.info("Get all data form table ENDS   , Total Columns " + columns.size() + "Total  fetch  records : "
					+ tableDataList.size() + "Time : " + LocalDateTime.now());
		} catch (Exception e) {
			log.error("getAllData from table fails !!!",e);
		}
		return tableDataList;
	}

	public TableResultSetExtractor(List<SQLColumn> columns) {
		super();
		this.columns = columns;
	}

	public TableResultSetExtractor() {
	}

	private static Object getColumnValue(ResultSet rs, String columnType, String ColumnName, int scale) {

		try {
			switch (columnType.toUpperCase()) {

			case "INTEGER": // B //only zero 4 digit
				return rs.getInt(ColumnName);

			
			case "SMALLINT":
				 return rs.getShort(ColumnName);  
				 
			case "DECIMAL": // P -> decimal // scale > 0 // vikas sir scale=6 or 2 // only 4 fields
			case "NUMERIC": // S -> decimal // scale > 0 // vikas sir scale=6 or 2 // only 4 fields
			case "BIGINT":
				if (scale > 0)
					return rs.getBigDecimal(ColumnName);
				return rs.getLong(ColumnName);
			
						
			case "DECFLOAT":
				return rs.getBigDecimal(ColumnName);
			
			case "REAL":
				return rs.getFloat(ColumnName);
				                
			case "DOUBLE":
				return rs.getDouble(ColumnName);
				
			case "CHAR":
			case "CHAR()":
			case "CHAR ()":
			case "CHARACTER":
			case "CHARACTER ()":
			case "CHARACTER()":
			case "CHARACTER VARYING":
			case "CHARACTER VARYING ()":
			case "CHARACTER VARYING()":
			case "NCHAR":
			case "NCHAR ()":
			case "NCHAR()":
			case "NCHAR VARYING":
			case "NCHAR VARYING ()":
			case "NCHAR VARYING()":
			case "VARCHAR":
			case "VARCHAR ()":
			case "VARCHAR()":
			case "NVARCHAR":
			case "NVARCHAR ()":
			case "NVARCHAR()":
				return rs.getString(ColumnName).trim().replace("\u0000", "");
				
						
			case "CHAR () FOR BIT DATA":
			case "VARCHAR () FOR BIT DATA":
			case "CHAR() FOR BIT DATA":
			case "VARCHAR() FOR BIT DATA":
				return rs.getBytes(ColumnName);
						
				
			case "BINARY":
			case "BINARY()":
			case "BINARY ()":
			case "VARBINARY":
			case "VARBINARY()":
			case "VARBINARY ()":
				return rs.getBytes(ColumnName); 
				
			case "BLOB ()":
			case "BLOB()":
			case "BLOB":
				return rs.getBlob(ColumnName);            //3
				
			case "GRAPHIC":
			case "GRAPHIC ()":
			case "GRAPHIC()":
			case "VARGRAPHIC":
			case "VARGRAPHIC ()":
			case "VARGRAPHIC()":	
				return rs.getString(ColumnName);           //4
				
			case "DOUBLE PRECISION":
			case "FLOAT ()":
			case "FLOAT()":
			case "FLOAT":   
				return rs.getBigDecimal(ColumnName);
				
			case "DATE":
				return rs.getDate(ColumnName);
		
			case "TIME":
				return rs.getTime(ColumnName);
				
			case "TIMESTAMP":
			case "TIMESTAMP ()":
			case "TIMESTAMP()":
				return rs.getTimestamp(ColumnName);
				
			
			case "CLOB":
			case "CLOB ()":
			case "CLOB()":
			case "DBCLOB":
			case "DBCLOB ()":
			case "DBCLOB()":
				return rs.getClob(ColumnName);
			
			default:
				return rs.getObject(ColumnName).toString();
		
			}
		} catch (Exception e) {
			log.error("column Type not available mismatch :" + e.getMessage(), e);
		}

		return null;
	}

}
