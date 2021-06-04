package com.as400datamigration.common;

public class Constant {

	// AS400 Query
	public static final String AS400_SELECT_TOTAL_ROW = "SELECT count(*) FROM ";

	public static final String AS400_SELECT_ALL_FROM = "SELECT * FROM ";
	// SELECT RRN(A), a.* FROM tushar/student A
	// offset 5 rows fetch next 5 rows only

	// postgres Query
	public static final String POSTGRES_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS %s.";

	public static final String POSTGRES_INSERT_INTO = "INSERT INTO %s.";

}
