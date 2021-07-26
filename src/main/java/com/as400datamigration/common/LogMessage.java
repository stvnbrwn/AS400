package com.as400datamigration.common;

public interface LogMessage {
	
	String APPLICATION_START_MSG = "*** Application Starts ***";
	
	String COMPLETE_MIGRATION_STARTS = "Starting of AS400_DATAMIGRATION full process. . .!";

	String OPT_MSG = "Waiting for your input : ";

	String INPUT_FILE_MSG = "Please enter input file : ";

	String ALIEN_CENTER = "                                           ";

	String SELECT_VALID_OPT = "Please select valid option !!!";

	String CONTINUE_MSG = "Press 'Y' to continue : ";

	String RETRY_MSG = "Retry with specified input.";

	String APPLICATION_CURRENT_SUMMARY_START_MSG = "*** Current Summary Module Starts ***";

	String FAILED_BATCH_INPUT_MSG = "Give input(1/2) for create failed batch :";

	String FAILED_BATCH_OPT1_MSG = "1) For create random 10 failed batch";

	String FAILED_BATCH_OPT2_MSG = "2) Create 4,5,6 batch no as failed batch";

	String FAILED_TABLES_SUMMERY_START= "*** Failed Table Row Summery Starts ***";

	String FAILED_TABLES_SUMMERY_END = "*** Failed Table Row Summery Ends ***";

	String PASSED_TABLES_ROWS_SUMMERY_STARTS = "*** Passed Table Row Summery Starts ***";

	String PASSED_TABLES_ROWS_SUMMERY_ENDS = "*** Passed Table Row Summery Ends ***";

	String NO_MISS_MATCH_ROWS_IN_FAILED_TABLES_MSG = "Please restart complete migration from starting.";

	String NO_MISS_MATCH_ROWS_IN_PASSED_TABLES = "All passed table's rows are matched";

	String RS_START_MSG = "*** Result Summary ***";

	String CURRENT_SUMMARY_COMPLETE = "*** Process Current Status Summary Complete ***";

	String PROCESS_SYNC_FINISH = "*** Process Sync Finished ***";

	String PROCESS_FAILED_BATCH_COMPLETE = "*** Process Failed Batches Finished ***";

	String PROCESS_COMPLETE_MIGRATION_COMPLETE = "*** Process Complete Migration Finished ***";

	String APP_STARTS_MSG_IN_CRON_MODULE = "*** Application Starts In Cron Module ***";

	String APP_ENDS_MSG_IN_CRON_MODULE = "*** Application Cron Module Ends ***";

	String APP_STARTS_MSG_IN_CONSOLE_MODULE = "*** Application Starts In Console Module ***";

	String APP_ENDS_MSG_IN_CONSOLE_MODULE = "*** Application Console Module Ends ***";

}
