package com.as400datamigration.runner;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.as400datamigration.audit.AuditMessage;
import com.as400datamigration.model.BatchDetail;
import com.as400datamigration.reposistory.PostgresDao;
import com.as400datamigration.service.As400DataMigrationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Profile("Full")
public class As400DataMigrationRunner implements CommandLineRunner {

	@Autowired
	As400DataMigrationService as400DataMigrationService;
	
	@Autowired
	PostgresDao postgresDao;

	@Override
	public void run(String... args) throws Exception {
		try {
			String filePath = null;
			if (args.length > 0)
				filePath = args[0];

			log.info("Starting of AS400_DATAMIGRATION Full Process. . .! " + LocalDateTime.now());
			processCompleteMigration(filePath);
			log.info("Ending   of AS400_DATAMIGRATION Full Process. . .! " + LocalDateTime.now());
			
			log.info("Starting of AS400_DATAMIGRATION Failed Process. . .! " + LocalDateTime.now());
			//processSyncInsert(filePath);
			log.info("Ending   of AS400_DATAMIGRATION Failed Process. . .! " + LocalDateTime.now());
			
			log.info("Starting of AS400_DATAMIGRATION Failed Process. . .! " + LocalDateTime.now());
			//processFailedBatches();
			log.info("Ending   of AS400_DATAMIGRATION Failed Process. . .! " + LocalDateTime.now());

		} catch (Exception e) {

			e.printStackTrace();
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			log.trace(exceptionAsString);

		}

	}

	private void processSyncInsert(String filePath) {
		// puri ki puri table missing h 
				//Auto_start for all table which are in table process?? or from input file
				//max_rrn
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
		/* what about fail table --> 
		 * Table_Not_Found_At_Source, Table_Desc_Not_Found_At_Source,
		 * Table_Creation_Failed,
		 */
		try { 
			List<BatchDetail> failedBatchList= postgresDao.getfailedbatch(); // doubt max attempt
			
			if(Objects.nonNull(failedBatchList)) {
				failedBatchList.forEach(batch->{
					as400DataMigrationService.processFailedBatches(batch);
				});
			}
		} catch (Exception e) {
			log.error(AuditMessage.Execption_Msg + e);
		}
	}

	private void processCompleteMigration(String filePath) {
		Path path;
		List<String> tableList = new ArrayList<>();
		try {
			if (!Objects.isNull(filePath)) {
				path = Paths.get(filePath);
				tableList = Files.readAllLines(path);
			}
			tableList.forEach(tableName -> {
				if (!tableName.isEmpty()) {
					as400DataMigrationService.processCompleteMigration(tableName);
				}
			});

		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
