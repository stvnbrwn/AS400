package com.as400datamigration.common;

public interface AuditMessage {
	
	String TABLE_STATE_FAILED="failed";
	
	String TABLE_STATE_SUCCESS="passed";
	
	String TABLE_STATE_FAILED_MESSAGE_AT_GETMETADATA = "this table may not exist at the source server... \n please check following things for more details :- \n";
	
	String TABLE_STATE_FAILED_MESSAGE_AT_CREATION = "this table exists at the source server, but creation failed ... \n please check following things for more details :- \n";
	
	String TABLE_STATE_SUCCESS_MESSAGE= "table creation successful.";
	
	//AllBatchDetail
	String ALL_BATCH_DETAIL_STATUS_RUNNING= "RUNNING";
	

}
