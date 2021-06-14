package com.as400datamigration.audit;

public enum BatchDetailStatus {
	
	Started_At_Source,
	Failed_At_Source,
	Ended_At_Source,
	
	Started_At_Destination,
	Failed_At_Destination,
	Ended_At_Destination,
	
	Batch_Refactored
	
	/*
	 * RUNNING, COMPLETED, FAILED
	 */
}
