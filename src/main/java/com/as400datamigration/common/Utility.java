package com.as400datamigration.common;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.as400datamigration.model.PostgresQueries;
import com.as400datamigration.model.SQLColumn;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class Utility {

	@Value("${postgres.schema}")
	private String schema;

	// AS400
	public String getRowCount(String tableName) {
		return String.format(Constant.AS400_SELECT_TOTAL_ROW, tableName);
	}

	public String fetchTableDesc(String tableName) {
		int index = tableName.indexOf(".");
		String tschema = tableName.substring(0, index);
		String tname = tableName.substring(index + 1);

		return String.format(Constant.AS400_SELECT_TABLE_DESC, tschema, tname);
	}

	/*
	 * public String getSelectQuery(String tableName) { return
	 * String.format(Constant.AS400_SELECT_ALL_FROM, tableName); }
	 */

	public String getSelectQueryForBatch(String tableName, long offset, long totalRecords) {
		return String.format(Constant.AS400_SELECT_ALL_IN_BATCH, tableName, offset, totalRecords);
	}

	// 4) TEST
	public String getSelectQueryFor5Records(String tableName) {

		return String.format(Constant.AS400_SELECT_FIRST_5_ROW, tableName);
	}

	public String getTableMetaData(String tableName) {
		return String.format(Constant.AS400_SELECT_TABLE_META_DATA, tableName);
	}

	// postgresql
	public String getPostgresDataType(SQLColumn sqlColumn) {
		try {
			switch (sqlColumn.getColumnType()) {

			case "CHAR":
				return "VARCHAR";

			case "DECIMAL": // P -> decimal // scale > 0 // vikas sir scale=6 or 2 // only 4 fields
			case "NUMERIC": // S -> decimal // scale > 0 // vikas sir scale=6 or 2 // only 4 fields
				if (sqlColumn.getScale() > 0)
					return "Numeric";
				return "bigInt";

			case "INTEGER": // B //only zero 4 digit
				return "bigInt";

			default:
				throw new Exception(sqlColumn.getColumnType());

			}
		} catch (Exception e) {
			log.error("column Type not available mismatch :" + e.getMessage());
			e.printStackTrace();
		}
		return null;

	}

	public PostgresQueries getPostgresQueries(String tableName, List<SQLColumn> columns) {

		String crtQuery = String.format(Constant.POSTGRES_CREATE_TABLE, schema)
				+ tableName.substring(tableName.lastIndexOf(".") + 1) + " ( ";
		String insertQuery = String.format(Constant.POSTGRES_INSERT_INTO, schema)
				+ tableName.substring(tableName.lastIndexOf(".") + 1) + " ( ";
		String aftrValues = "values ( ";

		for (SQLColumn sqlColumn : columns) {
			crtQuery += sqlColumn.getName().replace("#", "") + " " + getPostgresDataType(sqlColumn) + ",";
			insertQuery += sqlColumn.getName().replace("#", "") + " , ";
			aftrValues += " ?,";
		}
		// remove last comma
		crtQuery = crtQuery.substring(0, crtQuery.lastIndexOf(",")) + " ) ";

		insertQuery = insertQuery.substring(0, insertQuery.lastIndexOf(",")) + " ) ";
		aftrValues = aftrValues.substring(0, aftrValues.lastIndexOf(",")) + " ) ";

		return new PostgresQueries(crtQuery, insertQuery + aftrValues);

	}

	public String getAllTableProcess(Object object) {
		return String.format(Constant.POSTGRES_LOG_INTO_ALL_TABLE_PROCESS, schema);
	}

	public String getAllBatchDeatil(Object object) {
		return  String.format(Constant.POSTGRES_LOG_INTO_ALL_BATCH_DETAILS, schema);
	}

	public String getAllBatchDetail(BatchDetailStatus status) {
		
		return null;
	}

}
