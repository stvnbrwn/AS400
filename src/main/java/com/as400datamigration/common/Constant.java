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
	//String AS400_SELECT_ALL_FROM = "SELECT rrn(a) as rrn, a.* FROM %s a";
	
	// AS400 -> select all data
		String AS400_SELECT_ALL_IN_BATCH = "SELECT rrn(a) as rrn, a.* FROM %s a "
				+ "where rrn(a) between %s and %s";
	
	
	// postgres Query
	String P_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS %s.";

	String P_INSERT_INTO = "INSERT INTO %s.";
	
	String P_LOG_INTO_TABLE_PROCESS = "INSERT INTO %s.all_table_process (table_name,total_rows,status,reason) values (?,?,?,?)";
	
	String P_LOG_UPDATE_TABLE_PROCESS = "update %s.all_table_process set "
			+ "status = ?,"
			+ "reason = ? "
			+ "where table_name =?";
	         
	String P_LOG_INTO_BATCH_DETAILS="INSERT INTO %s.all_betch_details "
			+ "(table_name ,starting_rrn,ending_rrn ,started_at_source,started_at_destination,status,"
			+ "ended_at_source,ended_at_destination,modified_at,reason) values"
			+ "(?,?,?,?,?,?,?,?,?,?)";
	
	String P_LOG_UPDATE_BATCH_DETAILS="UPDATE %s.all_betch_details "
			+ "started_at_destination = ? ,"
			+ "status =? ,"
			+ "ended_at_source = ?,"
			+ "ended_at_destination=?,"
			+ "modified_at=?,"
			+ "reason=?"
			+ "where table_name= ? and starting_rrn = ? and ending_rrn= ?";
	
	String P_LOG_INTO_FAIL_BATCH_DETAILS="INSERT INTO %s.failed_betch_details "
			+ "(bno, table_name, starting_rrn, ending_rrn, started_at,attempts, ended_at, modified_at,reason) values"
			+ "(?,?,?,?,?,?,?,?,?)";

	
}
