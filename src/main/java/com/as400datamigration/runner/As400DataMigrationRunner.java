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

		while (start) {
			Scanner reader = new Scanner(System.in);
			try {
				if (!wrongInput)
					utility.printMainManu();
				
				System.out.print(LogMessage.OPT_MSG);
				int opt = reader.nextInt();

				switch (opt) {
				case 1:
					log.info(LogMessage.APPLICATION_START_MSG);
					System.out.print(LogMessage.INPUT_FILE_MSG);
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
									log.error("Failed Batch Processing Fail !!!", e);
								}
							}
						}
					} catch (IOException io) {
						log.error("File Not Found While Starting ...!", io);
					} catch (Exception e) {
						log.error("File Not Found While Starting ...!", e);
					}
					break;

				case 2:
					log.info(LogMessage.APPLICATION_START_MSG);
					System.out.print(LogMessage.INPUT_FILE_MSG);
					try {
						String filePath = reader.next();
						if (!filePath.trim().isEmpty()) {
							processSyncInsert(filePath);
						}
					} catch (Exception e) {
						log.error("File Not Found While Starting ...!", e);
					}
					break;

				case 3:
					log.info(LogMessage.APPLICATION_START_MSG);
					System.out.print(LogMessage.INPUT_FILE_MSG);
					try {
						String filePath = reader.next();
						if (!filePath.trim().isEmpty()) {
							processFailedBatches(utility.getInputFileData(filePath));
						}
					} catch (Exception e) {
						log.error("Failed Batch Processing Fail !!!", e);
					}
					break;

				case 4:
					log.info(LogMessage.APPLICATION_CURRENT_SUMMARY_START_MSG);
					System.out.print(LogMessage.INPUT_FILE_MSG);
					try {
						String filePath = reader.next();
						if (!filePath.trim().isEmpty()) {
							getCurrentStatusSummery(filePath);
						}
					} catch (Exception e) {
						log.error("File Not Found While Starting ...!", e);
					}
					break;

				case 5:
					utility.HelpManu();
					break;

				case 6:
					start = false;
					break;
					
				case 777:
					System.out.print(LogMessage.ALIEN_CENTER+LogMessage.FAILED_BATCH_OPT1_MSG);
					System.out.print(LogMessage.ALIEN_CENTER+LogMessage.FAILED_BATCH_OPT2_MSG);
					System.out.print(LogMessage.ALIEN_CENTER+LogMessage.FAILED_BATCH_INPUT_MSG);
					try {
						createFailBatch(reader.nextInt());
					} catch (Exception e) {
						log.error("Error While Creating failed batch ...!",e);
					}
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
				log.error("Error While Starting ...!",e);
				System.out.println(LogMessage.ALIEN_CENTER + LogMessage.RETRY_MSG);
			}finally {
				//reader.close();
			}
		}
		// System.out.println("exit");
	}

	private void createFailBatch(int i) {
			as400DataMigrationServiceTest.createfailedBatch(i);
	}

	private void getCurrentStatusSummery(String filePath) {
		List<String> tableList = new ArrayList<>();
		try {
			tableList = utility.getInputFileData(filePath);
			ExecutorService executor = Executors.newCachedThreadPool(); // takefrom app pro

			System.out.println(LogMessage.ALIEN_CENTER + "Total Tables : " + tableList.size());
			List<TableSummary> outPutList = new ArrayList<TableSummary>(tableList.size());

			Map<String, TableSummaryJson> tableSummaryMap = utility.getTableStatusMap();
			
			for (int i = 0; i < tableList.size(); i++) {
				As400DataMigrationServiceTest as400DataMigrationServiceTest = new As400DataMigrationServiceTest();
				applicationContext.getAutowireCapableBeanFactory().autowireBean(as400DataMigrationServiceTest);
				Future<TableSummary> futureCall = executor
						.submit(new CurrentTableStatusThread(tableList.get(i), as400DataMigrationServiceTest,
								tableSummaryMap));
				outPutList.add(i, futureCall.get());
			}
			executor.shutdown();
			while (!executor.isTerminated()) {
			}
			System.out.println(LogMessage.ALIEN_CENTER + "** Result Summary **");
			outPutList.forEach(tableSummary -> System.out.println(tableSummary));
			System.out.println(LogMessage.ALIEN_CENTER + "Process Current Status Summary Complete..!");

		} catch (FileNotFoundException e) {
			log.error("Table Summery Json File Not Found !!!", e);
		} catch (IOException e) {
			log.error("Input File Not Found !!!", e);
		} catch (Exception e) {
			log.error("Exception At Current Status Summary Process !!!");
		}

	}

	private void processSyncInsert(String filePath) {
		List<String> tableList = new ArrayList<>();
		try {
			tableList = utility.getInputFileData(filePath);
			ExecutorService executor = Executors.newFixedThreadPool(poolSize);
			System.out.println(LogMessage.ALIEN_CENTER + "Total Tables : " + tableList.size());

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
			System.out.println(LogMessage.ALIEN_CENTER + "Process Sync Finished ..!");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void processFailedBatches(List<String> tableList) {
		try {
			List<BatchDetail> failedBatchList = postgresDao.getfailedbatch(); // doubt max attempt
			ExecutorService executor = Executors.newFixedThreadPool(poolSize);
			System.out.println(LogMessage.ALIEN_CENTER + "Total Failed Batches : " + failedBatchList.size());
			if (Objects.nonNull(failedBatchList)) {
				List<Boolean> outPutList = new ArrayList<Boolean>(tableList.size());
				int i=0;
				for (String table : tableList) {
					List<BatchDetail> tableFailedBatch=failedBatchList.stream()
							.filter(batch->table.equalsIgnoreCase( batch.getTableName())).collect(Collectors.toList());
					if(!tableFailedBatch.isEmpty()) {
						As400DataMigrationService as400DataMigrationService = new As400DataMigrationService();
						applicationContext.getAutowireCapableBeanFactory().autowireBean(as400DataMigrationService);
						
						Future<Boolean> futureCall = executor
								.submit(new FailedBatchThread(tableFailedBatch, as400DataMigrationService));
						outPutList.add(i++, futureCall.get());
					}
					else {
						i++;
						outPutList.add(false);
					}
				}
				executor.shutdown();
				while (!executor.isTerminated()) {
				}
				i=0;
				for (Boolean allFailedBatchPass : outPutList) {
					if(allFailedBatchPass) {
						postgresDao.updateTableProcessStatus(new TableProcess(tableList.get(i),
								TableStatus.MIGRATION_SUCCESSFUL).getUpdateObjArray());
					}
					i++;
				}
				System.out.println(LogMessage.ALIEN_CENTER + "Process Failed Batches Finished ..!");
			}
		} catch (Exception e) {
			log.error(AuditMessage.EXECPTION_MSG + e);
		}
	}

	private void processCompleteMigration(List<String> tableList) {
		try {
			ExecutorService executor = Executors.newFixedThreadPool(poolSize); // takefrom app pro
			System.out.println(LogMessage.ALIEN_CENTER + "Total Tables : " + tableList.size());

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
			System.out.println(LogMessage.ALIEN_CENTER + "Process Complete Migration Finished ..!");
			// latch.await();
		} catch (Exception e) {
			log.error("File Not Found !!!", e);
		}

	}

	/*
	 * public class ThreadClass extends Thread { private CountDownLatch latch;
	 * private String tableName; private As400DataMigrationService
	 * as400DataMigrationService; private As400DataMigrationServiceTest
	 * as400DataMigrationServiceTest;
	 * 
	 * ThreadClass() { }
	 * 
	 * public ThreadClass(CountDownLatch latch, String tableName,
	 * As400DataMigrationService as400DataMigrationService) { super(); this.latch =
	 * latch; this.tableName = tableName; this.as400DataMigrationService =
	 * as400DataMigrationService; }
	 * 
	 * public ThreadClass(CountDownLatch latch, String tableName,
	 * As400DataMigrationServiceTest as400DataMigrationServiceTest) { super();
	 * this.latch = latch; this.tableName = tableName;
	 * this.as400DataMigrationServiceTest = as400DataMigrationServiceTest; }
	 * 
	 * public void run() {
	 * 
	 * if(Objects.nonNull(as400DataMigrationService))
	 * as400DataMigrationService.processCompleteMigration(tableName); else if
	 * (Objects.nonNull(as400DataMigrationServiceTest))
	 * as400DataMigrationServiceTest.startTesting(tableName); latch.countDown(); } }
	 */

	/*
	 * void data() { try { //String filePath = null;
	 * 
	 * if (args.length > 0) filePath = args[0];
	 * 
	 * // CompletableFuture.allOf();
	 * 
	 * tableList.forEach(tableName -> { if (!tableName.isEmpty()) {
	 * //As400DataMigrationService as400DataMigrationService= new
	 * As400DataMigrationService();
	 * //applicationContext.getAutowireCapableBeanFactory().autowireBean(
	 * as400DataMigrationService);
	 * as400DataMigrationService.processCompleteMigration(tableName); } });
	 * 
	 * 
	 * log.info("Starting of AS400_DATAMIGRATION Failed Process. . .! " +
	 * LocalDateTime.now()); // processSyncInsert(filePath);
	 * log.info("Ending   of AS400_DATAMIGRATION Failed Process. . .! " +
	 * LocalDateTime.now());
	 * 
	 * log.info("Starting of AS400_DATAMIGRATION Failed Process. . .! " +
	 * LocalDateTime.now()); // processFailedBatches();
	 * log.info("Ending   of AS400_DATAMIGRATION Failed Process. . .! " +
	 * LocalDateTime.now());
	 * 
	 * } catch (Exception e) {
	 * 
	 * e.printStackTrace(); StringWriter sw = new StringWriter();
	 * e.printStackTrace(new PrintWriter(sw)); String exceptionAsString =
	 * sw.toString(); log.trace(exceptionAsString);
	 * 
	 * } }
	 */
}
