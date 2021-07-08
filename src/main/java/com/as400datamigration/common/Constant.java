package com.as400datamigration.common;

public interface Constant {

	// AS400 Query
	String AS400_SELECT_TOTAL_ROW = "SELECT count(*) FROM %s";
	
	String AS400_SELECT_TABLE_META_DATA="select count(*) totalRows , min(RRN(a)) minRrn ,max(RRN(a)) "
			+ "maxRrn from %s a";
	
	String AS400_SELECT_TABLE_DESC="Select NAME,DATA_TYPE,LENGTH,COLUMN_HEADING,scale from "
			+ "qsys2.syscolumns where TABLE_SCHEMA= '%s' AND tbname = '%s' ";
	
	//AS400 - For TESTING POINT
	String AS400_SELECT_FIRST_5_ROW="select rrn(a) as rrn, a.* from %s a fetch first"
			+ " 5 rows only";

	// AS400 -> select all data
		String AS400_SELECT_ALL_IN_BATCH = "SELECT rrn(a) as rrn, a.* FROM %s a "
				+ "where rrn(a) between %s and %s";
	
	
	// postgres Query
	String P_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS %s.";

	String P_INSERT_INTO = "INSERT INTO %s.";
	
	String P_LOG_INTO_TABLE_PROCESS = 
			"INSERT INTO %s.all_table_process "
		  + "(table_name,total_rows,min_rrn,max_rrn,status,column_json,created_at) "
		  + "values (?,?,?,?,?,?::JSON,?)";
	
	
	String P_LOG_UPDATE_ALL_TABLE_PROCESS = "update %s.all_table_process set "
			+ "total_rows = ?, "
			+ "min_rrn=?, "
			+ "max_rrn= ?, "
			+ "status=?, "
			+ "column_json=? "
			+ "where table_name=?";
	
	String P_FETCH_FROM_TABLE_PROCESS = 
			"SELECT * FROM %s.all_table_process where all_table_process.table_name='%s'";
	
	String P_LOG_UPDATE_TABLE_PROCESS_STATUS = 
			  "update %s.all_table_process set "
			+ "status = ? "
			+ "where table_name =?";
	         
	String P_LOG_INTO_BATCH_DETAILS=
			  "INSERT INTO %s.all_batch_details "
			+ "(table_name ,starting_rrn,ending_rrn ,started_at_source,started_at_destination,status,"
			+ "ended_at_source,ended_at_destination,modified_at,reason) values"
			+ "(?,?,?,?,?,?,?,?,?,?)";
	
	String P_LOG_UPDATE_BATCH_DETAILS="UPDATE %s.all_batch_details set "
			+ "started_at_destination = ? ,"
			+ "status =? ,"
			+ "ended_at_source = ?,"
			+ "ended_at_destination=?,"
			+ "modified_at=?,"
			+ "reason=?,"
			+ "column_json=?"
			+ " where bno= ?";
	
	String P_FETCH_LAST_BATCH_FROM_BATCH_DETAIL = 
			"select * from %s.all_batch_details "
			+ "where all_batch_details.table_name= '%s' order by starting_rrn desc limit 1";
	 
	String P_LOG_INTO_FAILED_BATCH_DETAILS=
			  "INSERT INTO %s.failed_batch_details "
			+ "(bno, started_at,status, ended_at,reason) values"
			+ "(?,?,?,?,?)";
	
	String P_LOG_UPDATE_FAILED_BATCH_DETAILS=
			  "UPDATE %s.failed_batch_details "
			+ "set "
			+ "status=?,"
			+ "ended_at=?,"
			+ "reason=? "
			+ "where fbno =?";

	String P_FETCH_FAILED_BATCH = 
			    "SELECT * FROM %s.all_batch_details  "
			  + "WHERE status = 'FAILED_AT_DESTINATION' or status = 'FAILED_AT_SOURCE' or "
			  + "status ='MAX_ATTEMPTS_REACHED' ";

	String P_LOG_INSERT_INTO_ALL_TABLE_PROCESS_DETAILS = 
			"INSERT INTO %s.all_table_process_details "
					  + "(table_name,reason) "
					  + "values (?,?)";

	String P_LOG_UPDATE_ALL_TABLE_PROCESS_WITHOUT_COLUMNS =  "update %s.all_table_process set "
			+ "total_rows = ?, "
			+ "min_rrn=?, "
			+ "max_rrn= ?, "
			+ "status=? "
			+ "where table_name=?";

	String P_LOG_FETCH_ALL_BATCH = "select * from %s.all_batch_details order by random() limit 10";

	String P_LOG_UPDATE_BATCH_DETAILS_STATUS = "UPDATE %s.all_batch_details set "
			+ "status =? ,"
			+ "modified_at=? "
			+ "where bno= ?";

	String P_LOG_FETCH_FAILED_BATCH_ATTEMPT = "select count(*) from %s.failed_batch_details where bno=%s";

	String P_LOG_FETCH_ALL_BATCH_IN_LIST = "select * from %s.all_batch_details where bno in (";


}
