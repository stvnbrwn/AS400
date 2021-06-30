package com.as400datamigration.audit;

public interface AuditMessage {
	
	//TableProcess msgs
	String Table_Not_Found_At_Source_Msg="This table may not exist at the source ... ";
	
	String Table_Desc_Not_Found_At_Source_Msg = "We get execption while fetching table description ...";
	
	String Table_Creation_Failed_Msg="Table creation fail at destination...";
	
	String Table_Created_And_InRunning_Msg = "Table Created And InRunning.";
	
	String Table_Created_With_NO_Data_Msg="Table Created With NO Data";
	
	String Execption_Msg= "\n please check following things for more details :- \n";

	String Table_Found_At_Source_Msg = "This table exist at the source.";

	String Table_Found_At_Destination_Msg = "Table Found At Destination";

	String Table_Not_Found_At_Destination_Msg = "Table Not Found At Destination";

}
