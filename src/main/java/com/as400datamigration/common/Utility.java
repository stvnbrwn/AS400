package com.as400datamigration.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.stereotype.Component;

import com.as400datamigration.model.BatchDetail;
import com.as400datamigration.model.PostgresQueries;
import com.as400datamigration.model.SQLColumn;
import com.as400datamigration.model.SelectQryDesAndSrc;
import com.as400datamigration.model.TableMetaData;
import com.as400datamigration.model.TableSummaryJson;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class Utility {

	@Value("${postgres.schema}")
	private String schema;
	
	@Value("${postgres.audit.schema}")
	private String auditSchema;
	@Value("${main.menu}")
	private String mainManuFilePath;
	
	@Value("${help.menu}")
	private String helpManuFilePath;
	
	@Value("${special.chars.in.columns-name}")
	private String spclCharInColumnsName;
	
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

	public String getSelectQueryForBatch(String tableName, long offset, long totalRecords) {
		return String.format(Constant.AS400_SELECT_ALL_IN_BATCH, tableName, offset, totalRecords);
	}

	// 4) TEST
	public String getSelectQueryFor5Records(String tableName) {

		return String.format(Constant.AS400_SELECT_FIRST_5_ROW, tableName);
	}

	public String getTableMetaDataSource(String tableName) {
		return String.format(Constant.AS400_SELECT_TABLE_META_DATA, tableName);
	}

	// postgresql
	public String getPostgresDataType(SQLColumn sqlColumn) {
		try {
			switch (sqlColumn.getColumnType().toUpperCase()) {
			
			
			case "SMALLINT":
				return "SMALLINT";             	                
			
			case "INTEGER": // B //only zero 4 digit
				return "INTEGER";
				
			case "BIGINT":
			    return "BIGINT" ;       
			    
			case "DECIMAL": // P -> decimal // scale > 0 // vikas sir scale=6 or 2 // only 4 fields
			case "NUMERIC": // S -> decimal // scale > 0 // vikas sir scale=6 or 2 // only 4 fields
				if (sqlColumn.getScale() > 0)
					return "Numeric";
				return "bigInt";
				
			
			case "DECFLOAT":
				return "FLOAT";   // 1
			
			case "REAL":
				return "REAL";
				                
			case "DOUBLE":
				return "DOUBLE"; 
				
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
				return "VARCHAR"; 
				
			case "CHAR () FOR BIT DATA":
			case "VARCHAR () FOR BIT DATA":
			case "CHAR() FOR BIT DATA":
			case "VARCHAR() FOR BIT DATA":
				return "BYTEA";           //2
						
				
			case "BINARY":
			case "BINARY()":
			case "BINARY ()":
			case "VARBINARY":
			case "VARBINARY()":
			case "VARBINARY ()":
			case "BLOB ()":
			case "BLOB()":
			case "BLOB":
				return "BYTEA";            //3
				
			case "GRAPHIC":
			case "GRAPHIC ()":
			case "GRAPHIC()":
			case "VARGRAPHIC":
			case "VARGRAPHIC ()":
			case "VARGRAPHIC()":	
				return "VARCHAR";            //4
				
			case "DOUBLE PRECISION":
			case "FLOAT ()":
			case "FLOAT()":
			case "FLOAT":   
				return "DOUBLE PRECISION";
				
			case "DATE":
				return "DATE";
		
			case "TIME":
				return "TIME";
				
			case "TIMESTAMP":
			case "TIMESTAMP ()":
			case "TIMESTAMP()":
				return "TIMESTAMP";
				
			
			case "CLOB":
			case "CLOB ()":
			case "CLOB()":
			case "DBCLOB":
			case "DBCLOB ()":
			case "DBCLOB()":
				return "TEXT";
				
			default:
				return "VARCHAR";

			}
		} catch (Exception e) {
			log.error("column Type not available mismatch :" + e.getMessage());
			e.printStackTrace();
		}
		return null;

	}

	public PostgresQueries getPostgresQueries(TableMetaData tableMetaData) {

		String crtQuery = String.format(Constant.P_CREATE_TABLE, schema)
				+ tableMetaData.getTableName().substring(tableMetaData.getTableName().lastIndexOf(".") + 1) + " ( ";
		String insertQuery = String.format(Constant.P_INSERT_INTO, schema)
				+ tableMetaData.getTableName().substring(tableMetaData.getTableName().lastIndexOf(".") + 1) + " ( ";
		String aftrValues = "values ( ";
		
		Map<String,String> spclCharMap= getSpclCharMap(spclCharInColumnsName);

		for (SQLColumn sqlColumn : tableMetaData.getColumns()) {
			crtQuery += getColumnsName(sqlColumn.getName(),spclCharMap) + " " + getPostgresDataType(sqlColumn) + ",";
			insertQuery += getColumnsName(sqlColumn.getName(),spclCharMap) + " , ";
			aftrValues += " ?,";
		}
		// remove last comma
		crtQuery = crtQuery.substring(0, crtQuery.lastIndexOf(",")) + " ) ";

		insertQuery = insertQuery.substring(0, insertQuery.lastIndexOf(",")) + " ) ";
		aftrValues = aftrValues.substring(0, aftrValues.lastIndexOf(",")) + " ) ";

		return new PostgresQueries(crtQuery, insertQuery + aftrValues);

	}

	/**
	 * @param columnName
	 * @param spclCharMap 
	 * @return columnName after replace spcl character
	 */
	private String getColumnsName(String columnName, Map<String, String> spclCharMap) {
		for (Map.Entry<String,String> pair : spclCharMap.entrySet()) {
			columnName=columnName.replace(pair.getKey(), pair.getValue());
		}
		return columnName;
	}

	/**
	 * @param spclCharInColumnsName
	 * @return return Map which key contain spcl char and value has replacing char
	 */
	private Map<String, String> getSpclCharMap(String spclCharInColumnsName) {
		
		String[] spclCharPair=spclCharInColumnsName.split(",");
		Map<String,String> spclCharMap= new HashMap<>();
		
		for (String pair : spclCharPair) {
			spclCharMap.put(pair.substring(0, pair.indexOf(":")), pair.substring(pair.indexOf(":")+1));
		}
		return spclCharMap;
	}

	public String getInsertIntoTableProcess() {
		return String.format(Constant.P_LOG_INTO_TABLE_PROCESS, auditSchema);
	}
	
	public String getUpdateTableProcessStatus() {
		return String.format(Constant.P_LOG_UPDATE_TABLE_PROCESS_STATUS, auditSchema);
	}
	
	public String getTableProcessMetaData(String tableName) {
		return String.format(Constant.P_FETCH_FROM_TABLE_PROCESS, auditSchema, tableName);
	}
	
	public String getInsertIntoBatchDetail() {
		return String.format(Constant.P_LOG_INTO_BATCH_DETAILS, auditSchema);
	}

	public String getUpdateBatchDetail() {
		
		return String.format(Constant.P_LOG_UPDATE_BATCH_DETAILS, auditSchema);
		
	}

	public String fetchFailedbatch() {
		return String.format(Constant.P_FETCH_FAILED_BATCH, auditSchema);
	}
	
	
	public String getInsertIntoFailedBatch() {
		return String.format(Constant.P_LOG_INTO_FAILED_BATCH_DETAILS, auditSchema);
	}

	public String getUpdateFailedBatchDetail() {
		return String.format(Constant.P_LOG_UPDATE_FAILED_BATCH_DETAILS, auditSchema);
	}

	public PreparedStatementCreator getPrepareStatement(String insertIntoBatchDetail, Object[] allBatchDetails, String[] keyNameArray) {
				
		return new PreparedStatementCreator() {
			public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
				PreparedStatement ps = conn.prepareStatement(insertIntoBatchDetail, keyNameArray);
				for (int i = 1; i < allBatchDetails.length+1; i++) {
					ps.setObject(i, allBatchDetails[i-1]);
				}
				return ps;
			}
		};
	}

	
	public void printMainManu() throws IOException  {
		
		InputStream in = getClass().getResourceAsStream(mainManuFilePath);
        Stream<String> lines = new BufferedReader(new InputStreamReader(in)).lines();
        lines.forEach(System.out::println);
	}

	public void HelpManu() throws IOException {
		InputStream in = getClass().getResourceAsStream(helpManuFilePath);
        Stream<String> lines = new BufferedReader(new InputStreamReader(in)).lines();
        lines.forEach(System.out::println);
	}

	/**
	 * @return Query String which Insert data Into all_table_process_details
	 */
	public String getInsertIntoTableProcessDetail() {
		return String.format(Constant.P_LOG_INSERT_INTO_ALL_TABLE_PROCESS_DETAILS, auditSchema);
	}

	/**
	 * @return Query String which update all detail ( total_row, min_rrn, max_rrn, status, column_json )
	 */
	public String updateTableDeatil() {
		return String.format(Constant.P_LOG_UPDATE_ALL_TABLE_PROCESS, auditSchema);
	}
	
	/**
	 * @return Query String which update all detail ( total_row, min_rrn, max_rrn, status, column_json )
	 */
	public String updateTableDeatilWithoutCoulmns() {
		return String.format(Constant.P_LOG_UPDATE_ALL_TABLE_PROCESS_WITHOUT_COLUMNS, auditSchema);
	}

	/**
	 * @param tableName
	 * @return Query String which fetch last batch detail from all_batch_details table
	 */
	public String getlastBatchDetails(String tableName) {
		return String.format(Constant.P_FETCH_LAST_BATCH_FROM_BATCH_DETAIL, auditSchema, tableName);
	}

	/**
	 * @param filePath
	 * @throws IOException 
	 */
	public List<String> getInputFileData(String filePath) throws IOException {
		FileReader fr = new FileReader(new File(filePath));
        BufferedReader bf= new BufferedReader(fr);
        Stream<String> lines = bf.lines();
        List<String> tableList = lines.filter(p->!p.isEmpty()).map(table->table.trim()).collect(Collectors.toList());
        //remove duplicate tables from list
        Set<String> set = new HashSet<>(tableList);
        tableList.clear();
        tableList.addAll(set);
        
        fr.close();
        bf.close();
		return tableList;
	}

	public String getAllBatch() {
		return String.format(Constant.P_LOG_FETCH_ALL_BATCH, auditSchema);
	}

	public String getUpdateBatchDetailStatus() {
		return String.format(Constant.P_LOG_UPDATE_BATCH_DETAILS_STATUS, auditSchema);
	}

	public String getFailedBatchAttemptQry(BatchDetail batch) {
		return String.format(Constant.P_LOG_FETCH_FAILED_BATCH_ATTEMPT, auditSchema,batch.getBno());
	}

	public String getAllBatchFromList(List<Integer> batchNoList) {
		String sql=String.format(Constant.P_LOG_FETCH_ALL_BATCH_IN_LIST, auditSchema);
		for (Integer intdata : batchNoList) {
			sql=sql+intdata+", ";
		}
		sql=sql.substring(0,sql.lastIndexOf(","))+")";
		return sql;
	}

	/**
	 * @return Map<String, TableSummaryJson>
	 * @throws IOException 
	 */
	public Map<String, TableSummaryJson> getTableStatusMap() throws IOException {
		InputStream in = getClass().getResourceAsStream("/static/table_summary_json.txt");
		Stream<String> lines = new BufferedReader(new InputStreamReader(in)).lines();
		String jsonContent = lines.collect(Collectors.joining());
		Map<String, TableSummaryJson> map = new ObjectMapper().readValue(jsonContent, new TypeReference<Map<String, TableSummaryJson>>() {
        });
		return map;
	}

	/**
	 * @param tableList 
	 * @return both select queries for destination row count and source row count
	 * 
	 */
	public SelectQryDesAndSrc fetchSelectFromDestinationAndSource(List<String> tableList) {
		String selectDest="select count(*) as total_rows, ";
		String resDes="";
		String selectSource="select count(*) as total_rows, ";
		String resSource="";
		String unionAll=" union all ";
		for (String tableName : tableList) {
			resDes +=  (selectDest +"'"+tableName.substring(tableName.lastIndexOf(".")+1) +"' as table_name from "+
						 schema + tableName.substring(tableName.lastIndexOf(".")) +
						 unionAll);
			resSource += (selectSource + "'"+tableName.substring(tableName.lastIndexOf(".")+1) +"' as table_name from "+ tableName + unionAll);
		}
		selectDest = resDes.substring(0,resDes.lastIndexOf(unionAll));
		selectSource = resSource.substring(0,resSource.lastIndexOf(unionAll));
		
		SelectQryDesAndSrc selectQryDesAndSrc= new SelectQryDesAndSrc(selectDest,selectSource);
		return selectQryDesAndSrc;
	}

	
}
