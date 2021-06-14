package com.as400datamigration.audit;

public enum TableStatus {
	
	//Table_Not_Found,
	Table_Not_Found_At_Source,
	Table_Desc_Not_Found_At_Source,
	 
	Table_Creation_Failed,	
	Table_Created_With_NO_Data,
	Table_Created_And_InRunning,
	Table_Created_With_FailedBatch,
	Table_Created_And_AllBatchCompleted
	
}
