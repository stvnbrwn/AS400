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

	String APPLICATION_TSTING_MSG = "*** TESTING MODULE STARTS ***";

	String RETRY_MSG = "Retry with Specified Input.";

}
