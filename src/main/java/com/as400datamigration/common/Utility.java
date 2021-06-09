package com.as400datamigration.common;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.as400datamigration.model.SQLColumn;

@Component
public class Utility {

	@Value("${postgres.schema}")
	private String schema;
	
	//AS400 
	public String getRowCount(String tableName) {
		return String.format(Constant.AS400_SELECT_TOTAL_ROW , tableName);
	}
	
	public String fetchTableDesc(String tableName) {
		int index=tableName.indexOf(".");
		String tschema=tableName.substring(0,index);
		String tname=tableName.substring(index+1);
		
		return String.format(Constant.AS400_SELECT_TABLE_DESC,tschema,tname);
	}
	
	public String getSelectQuery(String tableName) {
		return String.format(Constant.AS400_SELECT_ALL_FROM, tableName);
	}
	
	public String getSelectQueryForBatch(String tableName, long offset, long totalRecords) {
		return String.format(Constant.AS400_SELECT_ALL_IN_BATCH, tableName,offset,totalRecords);
	}

	//4) TEST
	public String getSelectQueryFor5Records(String tableName) {
		
		return  String.format(Constant.AS400_SELECT_FIRST_5_ROW, tableName);
	}
	
	//postgresql
	public String getCreateQuery(String tableName, List<SQLColumn> columns) {
		String crtQuery = String.format(Constant.POSTGRES_CREATE_TABLE, schema) + tableName.substring(tableName.lastIndexOf(".")+1) + " ( ";

		for (SQLColumn sqlColumn : columns) {
			crtQuery += sqlColumn.getCreateString(); // override SQLColumn toString method
		}
		// remove last comma
		crtQuery = crtQuery.substring(0, crtQuery.lastIndexOf(",")) + " ) ";
		return crtQuery;
	}

	public  String getInsertQuery(String tableName, List<SQLColumn> columns) {
		String insertQuery = String.format(Constant.POSTGRES_INSERT_INTO, schema) + tableName.substring(tableName.lastIndexOf(".")+1) +" ( ";
		String aftrValues = "values ( ";
		for (SQLColumn sqlColumn : columns) {
			insertQuery += sqlColumn.getInsertString();
			aftrValues += " ?,";
		}
		insertQuery = insertQuery.substring(0, insertQuery.lastIndexOf(",")) + " ) ";
		aftrValues = aftrValues.substring(0, aftrValues.lastIndexOf(",")) + " ) ";
		return insertQuery + aftrValues;
	}

	
	public String getTableMetaData(String tableName) {
		return String.format(Constant.AS400_SELECT_TABLE_META_DATA, tableName);
	}


}
