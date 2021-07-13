package com.as400datamigration.common;

public interface LogMessage {
	
	String APPLICATION_START_MSG = "*** APPLICATION STARTS ***";
	
	String COMPLETE_MIGRATION_STARTS = "Starting of AS400_DATAMIGRATION Full Process. . .!";

	String OPT_MSG = "                                           waiting for your input : ";

	String INPUT_FILE_MSG = "                                           Please Enter Input File : ";

	String ALIEN_CENTER = "                                           ";

	String SELECT_VALID_OPT = "Please select valid option ...";

	String CONTINUE_MSG = "Press 'Y' to continue : ";

	String FINISH_PROCESS_MSG = "Process finish. Press any key to end...";

	String APPLICATION_TESTING_MSG = "*** TESTING MODULE STARTS ***";

	String RETRY_MSG = "Retry with Specified Input.";

	String APPLICATION_CURRENT_SUMMARY_START_MSG = "*** CURRENT SUMMARY MODULE STARTS ***";

	String FAILED_BATCH_INPUT_MSG = "Give input(1/2) for create failed batch :";

	String FAILED_BATCH_OPT1_MSG = "1) For create random 10 failed batch";

	String FAILED_BATCH_OPT2_MSG = "2) create 4,5,6 batch no as failed batch";

}
