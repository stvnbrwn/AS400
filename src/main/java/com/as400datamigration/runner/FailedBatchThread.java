package com.as400datamigration.runner;

import java.util.List;
import java.util.concurrent.Callable;

import com.as400datamigration.model.BatchDetail;
import com.as400datamigration.service.As400DataMigrationService;

public class FailedBatchThread implements Callable<Boolean> {
	
	List<BatchDetail> tableFailedBatch;
	As400DataMigrationService as400DataMigrationService;
	
	public FailedBatchThread(List<BatchDetail> tableFailedBatch,
			As400DataMigrationService as400DataMigrationService) {
		super();
		this.tableFailedBatch = tableFailedBatch;
		this.as400DataMigrationService = as400DataMigrationService;
	}
	
	@Override
	public Boolean call() throws Exception {
		return as400DataMigrationService.processFailedBatches(tableFailedBatch);
	}

}
