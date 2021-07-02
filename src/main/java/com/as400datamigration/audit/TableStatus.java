package com.as400datamigration.audit;

public enum TableStatus {
	
	//Table_Not_Found,
	/*
	 * Table_Process_Start, Table_Found_At_Source,
	 */
	TABLE_NOT_CREATED,
	
	TABLE_NOT_FOUND_AT_SOURCE,
	TABLE_DESC_NOT_FOUND_AT_SOURCE,
	TABLE_CREATION_FAILED,
	
	TABLE_CREATED_WITH_NO_DATA,
	TABLE_CREATED_AND_IN_RUNNING,
	
	TABLE_CREATED_AND_ALL_BATCH_COMPLETED, 
	
	TABLE_SYNC_SECCUSSFUL, 
	TABLE_SYNC_FAIL, 
	
	/* Sync_Failed_At_Source */
	/* Table_Created_With_FailedBatch, */
}
