package com.as400datamigration.runner;

import java.util.concurrent.Callable;

import com.as400datamigration.audit.TestOutPutStatus;
import com.as400datamigration.model.TableSummary;
import com.as400datamigration.service.As400DataMigrationServiceTest;

public class CurrentTableStatusThread  implements Callable<TableSummary>{
	
	String tableName;
	As400DataMigrationServiceTest as400DataMigrationServiceTest;
	
	public CurrentTableStatusThread(String tableName, As400DataMigrationServiceTest as400DataMigrationServiceTest) {
		this.tableName=tableName;
		this.as400DataMigrationServiceTest=as400DataMigrationServiceTest;
	}

	@Override
	public TableSummary call() throws Exception {
		
		return as400DataMigrationServiceTest.getTableSummary(tableName);
	}


}
