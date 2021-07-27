package com.as400datamigration.audit;

/** 
 * This class for
 * 
 * @author Programmers.io - Mohit Kachhwaha - 23-Jul-2021
 * 	
 * 			Modification - MohitKachhwaha - 23-Jul-2021
 *          
 */
public enum RunOption {
	COMPLETE_MIGRATION("-opt MIG"),
	SYNC("-opt SYN"),
	RE_EXECUTE_FAILED_BATCH("-opt EFB"),
	HELP("-opt HLP"), 
	GET_CURRENT_STATUS("-opt STATUS");	
	
	 public final String option;

	 /**
	 * @param label
	 */
	 private RunOption(String label) {
		 this.option = label;
	}

}
