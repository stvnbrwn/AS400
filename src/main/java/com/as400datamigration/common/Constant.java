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
	String AS400_SELECT_ALL_FROM = "SELECT rrn(a) as rrn, a.* FROM %s a";
	
	// AS400 -> select all data
		String AS400_SELECT_ALL_IN_BATCH = "SELECT rrn(a) as rrn, a.* FROM %s a "
				+ "where rrn(a) between %s and %s";
	
	// SELECT RRN(A), a.* FROM tushar/student A
	// offset 5 rows fetch next 5 rows only

	// postgres Query
	String POSTGRES_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS %s.";

	public static final String POSTGRES_INSERT_INTO = "INSERT INTO %s.";

}
