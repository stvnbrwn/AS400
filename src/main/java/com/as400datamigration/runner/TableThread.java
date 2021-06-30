package com.as400datamigration.runner;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import com.as400datamigration.common.LogMessage;
import com.as400datamigration.service.As400DataMigrationService;
import com.as400datamigration.service.As400DataMigrationServiceTest;

public class TableThread implements Runnable {

	String tableName;
	As400DataMigrationService as400DataMigrationService;

	/* As400DataMigrationServiceTest as400DataMigrationServiceTest; */
	/* final int barSize=20; */
	int totalThread;
	static int totalThreadcount = 1;
	/* int lineBarCount; */
	BigDecimal percentage;
	/* int result; */
	/*
	 * static String lineBarString = "|"; static String threadLineBarString="" ;
	 * static BigDecimal curBigDecimal;
	 */

	public TableThread(String tableName, As400DataMigrationService as400DataMigrationService, int totalThread) {
		this.tableName = tableName;
		this.as400DataMigrationService = as400DataMigrationService;
		this.totalThread = totalThread;
		this.percentage = new BigDecimal(1).divide(new BigDecimal(totalThread), 5, RoundingMode.FLOOR)
				.multiply(new BigDecimal(100));
		/* this.lineBarCount=barSize/totalThread; */

	}

	/*
	 * public TableThread(String tableName, As400DataMigrationServiceTest
	 * as400DataMigrationServiceTest, int totalThread, Integer result) {
	 * this.tableName = tableName; this.as400DataMigrationServiceTest =
	 * as400DataMigrationServiceTest; this.totalThread = totalThread;
	 * this.percentage = new BigDecimal(1).divide(new BigDecimal(totalThread), 5,
	 * RoundingMode.FLOOR) .multiply(new BigDecimal(100)); this.result=result; }
	 */

	@Override
	public void run() {
		 as400DataMigrationService.processCompleteMigration(tableName);

		/*
		 * if (Objects.nonNull(as400DataMigrationService))
		 * as400DataMigrationService.processCompleteMigration(tableName);
		 */
		/*
		 * else if (Objects.nonNull(as400DataMigrationServiceTest))
		 * as400DataMigrationServiceTest.getTableSummary(tableName,);
		 */

		/*
		 * for (int i = 0; i <lineBarCount; i++) { lineBarString+=lineBarString; }
		 * 
		 * threadLineBarString = threadLineBarString + lineBarString;
		 * System.out.println(LogMessage.ALIEN_CENTER + "STATUS [ " +
		 * threadLineBarString );
		 */
		if (totalThread < totalThreadcount) {
			totalThreadcount = 1;
		}

		BigDecimal curStatus = percentage.multiply(new BigDecimal(totalThreadcount++)).setScale(0, RoundingMode.UP);
		if (curStatus.compareTo(new BigDecimal(100)) > 0) {
			curStatus = new BigDecimal(100);
		}
		//System.out.println(LogMessage.ALIEN_CENTER + "Processed so far : " + curStatus.setScale(0) + " % ");

	}

}
