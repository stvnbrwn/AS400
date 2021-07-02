package com.as400datamigration.audit;

public enum BatchDetailStatus {
	
	STARTED_AT_SOURCE,
	FAILED_AT_SOURCE,
	ENDED_AT_SOURCE,
	
	STARTED_AT_DESTINATION,
	FAILED_AT_DESTINATION,
	ENDED_AT_DESTINATION,
	
	BATCH_REFACTORED
	
}
