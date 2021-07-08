package com.as400datamigration.runner;

import java.util.List;

import com.as400datamigration.audit.ProgramOption;
import com.as400datamigration.audit.TableStatus;
import com.as400datamigration.model.BatchDetail;
import com.as400datamigration.model.TableMetaData;
import com.as400datamigration.service.As400DataMigrationService;

public class TableThread implements Runnable {

	String tableName;
	As400DataMigrationService as400DataMigrationService;
	ProgramOption programOption;
	List<BatchDetail> batchDetailList;

	public TableThread(String tableName, As400DataMigrationService as400DataMigrationService , ProgramOption programOption) {
		this.tableName = tableName;
		this.as400DataMigrationService = as400DataMigrationService;
		this.programOption=programOption;
	}

	@Override
	public void run() {
		if(programOption.equals(ProgramOption.PERFORM_COMPLETE_MIGRATION))
			as400DataMigrationService.processCompleteMigration(new TableMetaData(tableName), TableStatus.TABLE_NOT_CREATED);
		else if(programOption.equals(ProgramOption.PERFORM_SYNC))
			as400DataMigrationService.processSyncInsert(tableName);
		
	}

}
