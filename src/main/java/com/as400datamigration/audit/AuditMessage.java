package com.as400datamigration.audit;

public interface AuditMessage {

	// TableProcess msgs
	String TABLE_NOT_FOUND_AT_SOURCE_MSG = "This table may not exist at the source ... ";

	String TABLE_DESC_NOT_FOUND_AT_SOURCE_MSG = "We get execption while fetching table description ...";

	String TABLE_CREATION_FAILED_MSG = "Table is available at source and table description is all found but Table creation fail at destination...";

	String TABLE_CREATED_AND_INRUNNING_MSG = "Table Created And InRunning.";

	String TABLE_CREATED_WITH_NO_DATA_MSG = "Table Created With NO Data";

	String EXECPTION_MSG = "\n please check following things for more details :- \n";

	String TABLE_FOUND_AT_SOURCE_MSG = "This table exist at the source.";

	String TABLE_FOUND_AT_DESTINATION_MSG = "Table Found At Destination";

	String TABLE_NOT_FOUND_AT_DESTINATION_MSG = "Table Not Found At Destination";

	String SYNC_FAIL_AT_SOURCE_MSG = "SYNC FAIL AT SOURCE :- ";
	

}
