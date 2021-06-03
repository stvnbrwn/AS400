package com.as400datamigration.common;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;

import com.as400datamigration.model.SQLColumn;

public class Utility {

	@Value("${postgres.schema}")
	private static int schema;

	public static String getRowCount(String tableName) {
		return Constant.AS400_SELECT_TOTAL_ROW + tableName;
	}

	public static String getSelectQuery(String tableName) {
		return Constant.AS400_SELECT_ALL_FROM + tableName;
	}

	public static String getCreateQuery(String tableName, List<SQLColumn> columns) {

		/*
		 * CREATE TABLE public.d ( a character(8), b numeric(6) );
		 */

		String crtQuery = String.format(Constant.POSTGRES_CREATE_TABLE, schema) + tableName + " ( ";

		for (SQLColumn sqlColumn : columns) {
			crtQuery += sqlColumn.getCreateString(); // override SQLColumn toString method
		}

		// remove last comma
		crtQuery = crtQuery.substring(0, crtQuery.lastIndexOf(",")) + " ) ";

		return crtQuery;

	}

	public static String getInsertQuery(String tableName, List<SQLColumn> columns) {

		String insertQuery = String.format(Constant.POSTGRES_INSERT_INTO, schema) + " ( ";
		String aftrValues = "values ( ";

		for (SQLColumn sqlColumn : columns) {
			insertQuery += sqlColumn.getInsertString();
			aftrValues += " ?,";
		}

		insertQuery = insertQuery.substring(0, insertQuery.lastIndexOf(",")) + " ) ";

		aftrValues = aftrValues.substring(0, aftrValues.lastIndexOf(",")) + " ) ";

		return insertQuery + aftrValues;
	}

}
