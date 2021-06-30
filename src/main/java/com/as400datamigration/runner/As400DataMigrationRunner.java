package com.as400datamigration.runner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.as400datamigration.audit.AuditMessage;
import com.as400datamigration.common.LogMessage;
import com.as400datamigration.common.Utility;
import com.as400datamigration.model.BatchDetail;
import com.as400datamigration.model.TableSummary;
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
			try {
				if (!wrongInput)
					utility.printMainManu();
				Scanner reader = new Scanner(System.in);

				System.out.print(LogMessage.OPT_MSG);
				int opt = reader.nextInt();

				switch (opt) {
				case 1:

					log.info(LogMessage.APPLICATION_START_MSG);

					System.setProperty("spring.profiles.active", "configuration/application.properties");
					System.out.print(LogMessage.INPUT_FILE_MSG);
					try {
						String filePath = reader.next();
						if (!filePath.trim().isEmpty()) {
							processCompleteMigration(filePath);
						}
					} catch (Exception e) {
						log.error("File Not Found While Starting ...!", e);
					}
					break;

				/*
				 * case 2: break;
				 * 
				 * case 3: break;
				 */

				case 4:
					log.info(LogMessage.APPLICATION_TSTING_MSG);
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

				default:
					wrongInput = true;
					System.out.println(LogMessage.ALIEN_CENTER + LogMessage.SELECT_VALID_OPT);
					break;
				}
				System.out.print(LogMessage.ALIEN_CENTER + LogMessage.CONTINUE_MSG);
				if (!reader.next().equalsIgnoreCase("y"))
					start = false;
			} catch (Exception e) {
				log.error("Error While Starting ...!");
				System.out.println(LogMessage.ALIEN_CENTER + LogMessage.RETRY_MSG);
			}
		}
		// System.out.println("exit");
	}

	private void getCurrentStatusSummery(String filePath) {
		List<String> tableList = new ArrayList<>();
		try {
			FileReader fr = new FileReader(new File(filePath));
	        BufferedReader bf= new BufferedReader(fr);
	        Stream<String> lines = bf.lines();
	        tableList=lines.filter(p->!p.isEmpty()).collect(Collectors.toList());
	        fr.close();
	        bf.close();
	        ExecutorService executor = Executors.newCachedThreadPool(); // takefrom app pro
	        
			System.out.println(LogMessage.ALIEN_CENTER+ "Total Tables : " + tableList.size());
			List<TableSummary> outPutList = new ArrayList<TableSummary>(tableList.size());

			for (int i = 0; i < tableList.size(); i++) {
					As400DataMigrationServiceTest as400DataMigrationServiceTest = new As400DataMigrationServiceTest();
					applicationContext.getAutowireCapableBeanFactory().autowireBean(as400DataMigrationServiceTest);
					Future<TableSummary> futureCall = executor.submit(new CurrentTableStatusThread(tableList.get(i),
							as400DataMigrationServiceTest));
					outPutList.add(i, futureCall.get());
			}
			executor.shutdown();
			while (!executor.isTerminated()) {
			}
			System.out.println(LogMessage.ALIEN_CENTER +  " ** Result Summary **");
			outPutList.forEach(tableSummary->System.out.println(tableSummary));
			System.out.println(LogMessage.ALIEN_CENTER + "Process Current Status Summary Complete..!");

		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void processSyncInsert(String filePath) {
		// puri ki puri table missing h
		// Auto_start for all table which are in table process?? or from input file
		// max_rrn
		Path path;
		List<String> tableList = new ArrayList<>();
		try {
			if (!Objects.isNull(filePath)) {
				path = Paths.get(filePath);
				tableList = Files.readAllLines(path);
			}
			tableList.forEach(tableName -> {
				if (!tableName.isEmpty()) {
					as400DataMigrationService.processSyncInsert(tableName);
				}
			});

		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void processFailedBatches() {
		/*
		 * what about fail table --> Table_Not_Found_At_Source,
		 * Table_Desc_Not_Found_At_Source, Table_Creation_Failed,
		 */
		try {
			List<BatchDetail> failedBatchList = postgresDao.getfailedbatch(); // doubt max attempt

			if (Objects.nonNull(failedBatchList)) {
				failedBatchList.forEach(batch -> {
					as400DataMigrationService.processFailedBatches(batch);
				});
			}
		} catch (Exception e) {
			log.error(AuditMessage.Execption_Msg + e);
		}
	}

	private void processCompleteMigration(String filePath) {
		List<String> tableList = new ArrayList<>();
		try {	
			
	        FileReader fr = new FileReader(new File(filePath));
	        BufferedReader bf= new BufferedReader(fr);
	        Stream<String> lines = bf.lines();
	        tableList=lines.filter(p->!p.isEmpty()).collect(Collectors.toList());
	        fr.close();
	        bf.close();
			ExecutorService executor = Executors.newFixedThreadPool(poolSize); // takefrom app pro
			System.out.println(LogMessage.ALIEN_CENTER+ "Total Tables : " + tableList.size());

			for (int i = 0; i < tableList.size(); i++) {
					As400DataMigrationService as400DataMigrationService = new As400DataMigrationService();
					applicationContext.getAutowireCapableBeanFactory().autowireBean(as400DataMigrationService);
					Runnable tableThread = new TableThread(tableList.get(i), as400DataMigrationService,
							tableList.size());
					executor.execute(tableThread);
			}
			executor.shutdown();
			while (!executor.isTerminated()) {
			}
			System.out.println(LogMessage.ALIEN_CENTER + "Complete Migration Finished ..!");
			// latch.await();
		} catch (Exception e) {
			log.error("File Not Found !!!",e);
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
