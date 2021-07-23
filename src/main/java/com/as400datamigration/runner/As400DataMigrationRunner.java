package com.as400datamigration.runner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.as400datamigration.audit.AuditMessage;
import com.as400datamigration.audit.ProgramOption;
import com.as400datamigration.audit.TableStatus;
import com.as400datamigration.common.LogMessage;
import com.as400datamigration.common.Utility;
import com.as400datamigration.model.BatchDetail;
import com.as400datamigration.model.SelectQryDesAndSrc;
import com.as400datamigration.model.TableProcess;
import com.as400datamigration.model.TableSummary;
import com.as400datamigration.model.TableSummaryJson;
import com.as400datamigration.reposistory.PostgresDao;
import com.as400datamigration.service.As400DataMigrationService;
import com.as400datamigration.service.As400DataMigrationServiceTest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class As400DataMigrationRunner implements CommandLineRunner {

	@Autowired
	As400DataMigrationService as400DataMigrationService;

	@Autowired
	As400DataMigrationServiceTest as400DataMigrationServiceTest;

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	PostgresDao postgresDao;

	@Autowired
	Utility utility;

	@Value("${pool.size}")
	private int poolSize;

	@Override
	public void run(String... args) throws Exception {
		boolean start = true;
		boolean wrongInput = false;
		Scanner reader = new Scanner(System.in);
		
		while (start) {
			try {
				if (!wrongInput)
					utility.printMainManu();

				System.out.print(LogMessage.ALIEN_CENTER+LogMessage.OPT_MSG);
				int opt = reader.nextInt();

				switch (opt) {
				case 1:
					log.info(LogMessage.APPLICATION_START_MSG);
					System.out.print(LogMessage.ALIEN_CENTER + LogMessage.INPUT_FILE_MSG);
					try {
						String filePath = reader.next();
						if (!filePath.trim().isEmpty()) {
							List<String> tableList = new ArrayList<>();
							tableList = utility.getInputFileData(filePath);
							if (Objects.nonNull(tableList) && !tableList.isEmpty()) {
								processCompleteMigration(tableList);
								// perform failed batch ----> automaitcally
								try {
									processFailedBatches(tableList);
								} catch (Exception e) {
									log.error("Failed batch processing fail !!!", e);
								}
							}
						}
					} catch (IOException io) {
						log.error("File not found while starting ...!", io);
					} catch (Exception e) {
						log.error("Error at starting ...!", e);
					}
					break;

				case 2:
					log.info(LogMessage.APPLICATION_START_MSG);
					System.out.print(LogMessage.ALIEN_CENTER + LogMessage.INPUT_FILE_MSG);
					try {
						String filePath = reader.next();
						if (!filePath.trim().isEmpty()) {
							List<String> tableList = new ArrayList<>();
							tableList = utility.getInputFileData(filePath);
							if (Objects.nonNull(tableList) && !tableList.isEmpty()) {
								processSyncInsert(tableList);
								// perform failed batch ----> automaitcally
								try {
									processFailedBatches(tableList);
								} catch (Exception e) {
									log.error("Failed batch processing fail !!!", e);
								}
							}
						}
					}catch (IOException io) {
						log.error("File not found while starting ...!", io);
					} catch (Exception e) {
						log.error("Error at starting ...!", e);
					}
					break;

				case 3:
					log.info(LogMessage.APPLICATION_START_MSG);
					System.out.print(LogMessage.ALIEN_CENTER + LogMessage.INPUT_FILE_MSG);
					try {
						String filePath = reader.next();
						if (!filePath.trim().isEmpty()) {
							processFailedBatches(utility.getInputFileData(filePath));
						}
					} catch (Exception e) {
						log.error("Failed batch processing fail !!!", e);
					}
					break;

				case 4:
					log.info(LogMessage.APPLICATION_CURRENT_SUMMARY_START_MSG);
					System.out.print(LogMessage.ALIEN_CENTER +LogMessage.INPUT_FILE_MSG);
					try {
						String filePath = reader.next();
						if (!filePath.trim().isEmpty()) {
							getCurrentStatusSummery(filePath);
						}
					} catch (Exception e) {
						log.error("File not found while starting ...!", e);
					}
					break;

				case 5:
					utility.HelpManu();
					break;

				case 6:
					start = false;
					break;

				default:
					wrongInput = true;
					System.out.println(LogMessage.ALIEN_CENTER + LogMessage.SELECT_VALID_OPT);
					break;
				}
				System.out.print(LogMessage.ALIEN_CENTER + LogMessage.CONTINUE_MSG);
				if (!reader.next().equalsIgnoreCase("y"))
					start = false;
			} catch (Exception e) {
				log.error("Error while starting ...!", e);
				System.out.println(LogMessage.ALIEN_CENTER + LogMessage.RETRY_MSG);
			} 
		}
			reader.close();
	}

	/**
	 * @param filePath
	 */
	private void verifyTableRow(String filePath) {
		List<String> tableList = new ArrayList<>();
		try {
			tableList = utility.getInputFileData(filePath);
			SelectQryDesAndSrc selectQryDesAndSrc = utility.fetchSelectFromDestinationAndSource(tableList);
			boolean hasMissMatchedRows = as400DataMigrationServiceTest.runSelectDesAndSource(selectQryDesAndSrc,
					tableList);
			if (!hasMissMatchedRows) {
				System.out.println(LogMessage.NO_MISS_MATCH_ROWS_IN_PASSED_TABLES);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void createFailBatch(int i) {
		as400DataMigrationServiceTest.createfailedBatch(i);
	}

	private void getCurrentStatusSummery(String filePath) {
		List<String> tableList = new ArrayList<>();
		try {
			tableList = utility.getInputFileData(filePath);
			ExecutorService executor = Executors.newCachedThreadPool(); // takefrom app pro

			System.out.println(LogMessage.ALIEN_CENTER + "Total tables : " + tableList.size());
			List<TableSummary> outPutList = new ArrayList<TableSummary>(tableList.size());

			Map<String, TableSummaryJson> tableSummaryMap = utility.getTableStatusMap();

			for (int i = 0; i < tableList.size(); i++) {
				As400DataMigrationServiceTest as400DataMigrationServiceTest = new As400DataMigrationServiceTest();
				applicationContext.getAutowireCapableBeanFactory().autowireBean(as400DataMigrationServiceTest);
				Future<TableSummary> futureCall = executor.submit(
						new CurrentTableStatusThread(tableList.get(i), as400DataMigrationServiceTest, tableSummaryMap));
				outPutList.add(i, futureCall.get());
			}
			executor.shutdown();
			while (!executor.isTerminated()) {
			}
			System.out.println(LogMessage.ALIEN_CENTER + LogMessage.RS_START_MSG);
			int sno=1;
			for (TableSummary tableSummary : outPutList) {
				System.out.println("S.no. : "+ sno++ + " : "+tableSummary);
			}
			
			getRowSummery(outPutList);

			System.out.println(LogMessage.ALIEN_CENTER + LogMessage.CURRENT_SUMMARY_COMPLETE);

		} catch (FileNotFoundException e) {
			log.error("Table Summery Json File Not Found !!!", e);
		} catch (IOException e) {
			log.error("Input File Not Found !!!", e);
		} catch (Exception e) {
			log.error("Exception At Current Status Summary Process !!!", e);
		}

	}

	private void getRowSummery(List<TableSummary> outPutList) {
		try {
			List<String> outPutListOfFailTables = outPutList.stream().filter(
					tableSummary -> tableSummary.getTableStatus().equals(TableStatus.MIGRATION_PROCESS_IN_RUNNING)
							|| tableSummary.getTableStatus().equals(TableStatus.MIGRATION_SYNC_FAIL)
							|| tableSummary.getTableStatus().equals(TableStatus.MIGRATION_FAILED))
					.map(tableSummary -> tableSummary.getTableName()).collect(Collectors.toList());

			if (!outPutListOfFailTables.isEmpty()) {
				SelectQryDesAndSrc selectQryDesAndSrc = utility.fetchSelectFromDestinationAndSource(outPutListOfFailTables);
				System.out.println(LogMessage.ALIEN_CENTER + LogMessage.FAILED_TABLES_SUMMERY_START);
				System.out.println("Failed tables includs these status :-"
						+ TableStatus.MIGRATION_PROCESS_IN_RUNNING.toString() + " , "
						+ TableStatus.MIGRATION_SYNC_FAIL.toString() + " , " + TableStatus.MIGRATION_FAILED.toString());
				boolean hasMissMatchedRows = as400DataMigrationServiceTest.runSelectDesAndSource(selectQryDesAndSrc,
						outPutListOfFailTables);
				if (!hasMissMatchedRows)
					System.out.println(LogMessage.ALIEN_CENTER + LogMessage.NO_MISS_MATCH_ROWS_IN_FAILED_TABLES_MSG);
				System.out.println(LogMessage.ALIEN_CENTER + LogMessage.FAILED_TABLES_SUMMERY_END);
			}

			List<String> outPutListOfPassTables = outPutList.stream()
					.filter(tableSummary -> tableSummary.getTableStatus().equals(TableStatus.TABLE_CREATED_WITH_NO_DATA)
							|| tableSummary.getTableStatus().equals(TableStatus.MIGRATION_SUCCESSFUL)
							|| tableSummary.getTableStatus().equals(TableStatus.MIGRATION_SYNC_SUCCESSFUL))
					.map(tableSummary -> tableSummary.getTableName()).collect(Collectors.toList());

			if (!outPutListOfPassTables.isEmpty()) {

				SelectQryDesAndSrc selectQryDesAndSrc = utility.fetchSelectFromDestinationAndSource(outPutListOfPassTables);
				System.out.println(LogMessage.ALIEN_CENTER + LogMessage.PASSED_TABLES_ROWS_SUMMERY_STARTS);
				System.out.println(
						"Passed tables includs these status :-" + TableStatus.TABLE_CREATED_WITH_NO_DATA.toString()
								+ " , " + TableStatus.MIGRATION_SUCCESSFUL.toString() + " , "
								+ TableStatus.MIGRATION_SYNC_SUCCESSFUL.toString());
				boolean hasMissMatchedRows = as400DataMigrationServiceTest.runSelectDesAndSource(selectQryDesAndSrc,
						outPutListOfPassTables);

				if (!hasMissMatchedRows)
					System.out.println(LogMessage.ALIEN_CENTER + LogMessage.NO_MISS_MATCH_ROWS_IN_PASSED_TABLES);
				System.out.println(LogMessage.ALIEN_CENTER + LogMessage.PASSED_TABLES_ROWS_SUMMERY_ENDS);
			}
		} catch (Exception e) {
			throw e;
		}

	}

	private void processSyncInsert(List<String> tableList) {
		try {
			ExecutorService executor = Executors.newFixedThreadPool(poolSize);
			System.out.println(LogMessage.ALIEN_CENTER + "Total tables : " + tableList.size());

			tableList.forEach(tableName -> {
				As400DataMigrationService as400DataMigrationService = new As400DataMigrationService();
				applicationContext.getAutowireCapableBeanFactory().autowireBean(as400DataMigrationService);
				Runnable tableThread = new TableThread(tableName, as400DataMigrationService,
						ProgramOption.PERFORM_SYNC);
				executor.execute(tableThread);
			});
			executor.shutdown();
			while (!executor.isTerminated()) {
			}
			System.out.println(LogMessage.ALIEN_CENTER + LogMessage.PROCESS_SYNC_FINISH);
		}  catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void processFailedBatches(List<String> tableList) {
		try {
			List<BatchDetail> failedBatchList = postgresDao.getfailedbatch(); // doubt max attempt
			ExecutorService executor = Executors.newFixedThreadPool(poolSize);
			System.out.println(LogMessage.ALIEN_CENTER + "Total failed batches : " + failedBatchList.size());
			if (Objects.nonNull(failedBatchList)) {
				List<Boolean> outPutList = new ArrayList<Boolean>(tableList.size());
				int i = 0;
				for (String table : tableList) {
					List<BatchDetail> tableFailedBatch = failedBatchList.stream()
							.filter(batch -> table.equalsIgnoreCase(batch.getTableName())).collect(Collectors.toList());
					if (!tableFailedBatch.isEmpty()) {
						As400DataMigrationService as400DataMigrationService = new As400DataMigrationService();
						applicationContext.getAutowireCapableBeanFactory().autowireBean(as400DataMigrationService);

						Future<Boolean> futureCall = executor
								.submit(new FailedBatchThread(tableFailedBatch, as400DataMigrationService));
						outPutList.add(i++, futureCall.get());
					} else {
						i++;
						outPutList.add(false);
					}
				}
				executor.shutdown();
				while (!executor.isTerminated()) {
				}
				i = 0;
				for (Boolean allFailedBatchPass : outPutList) {
					if (allFailedBatchPass) {
						postgresDao.updateTableProcessStatus(
								new TableProcess(tableList.get(i), TableStatus.MIGRATION_SUCCESSFUL)
										.getUpdateObjArray());
					}
					i++;
				}
				System.out.println(LogMessage.ALIEN_CENTER + LogMessage.PROCESS_FAILED_BATCH_COMPLETE);
			}
		} catch (Exception e) {
			log.error(AuditMessage.EXECPTION_MSG + e);
		}
	}

	private void processCompleteMigration(List<String> tableList) {
		try {
			ExecutorService executor = Executors.newFixedThreadPool(poolSize); // takefrom app pro
			System.out.println(LogMessage.ALIEN_CENTER + "Total tables : " + tableList.size());

			for (int i = 0; i < tableList.size(); i++) {
				As400DataMigrationService as400DataMigrationService = new As400DataMigrationService();
				applicationContext.getAutowireCapableBeanFactory().autowireBean(as400DataMigrationService);
				Runnable tableThread = new TableThread(tableList.get(i), as400DataMigrationService,
						ProgramOption.PERFORM_COMPLETE_MIGRATION);
				executor.execute(tableThread);
			}
			executor.shutdown();
			while (!executor.isTerminated()) {
			}
			System.out.println(LogMessage.ALIEN_CENTER + LogMessage.PROCESS_COMPLETE_MIGRATION_COMPLETE);
		} catch (Exception e) {
			log.error("File Not Found !!!", e);
		}

	}

}
