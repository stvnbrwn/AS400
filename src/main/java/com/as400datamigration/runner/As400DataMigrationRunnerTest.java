package com.as400datamigration.runner;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.as400datamigration.service.As400DataMigrationServiceTest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Profile("test")
public class As400DataMigrationRunnerTest implements CommandLineRunner {

	/*
	 * @Autowired EmailSender emailsender;
	 */
	@Autowired
	As400DataMigrationServiceTest as400DataMigrationServiceTest;

	@Override
	public void run(String... args) throws Exception {

		try {

			String filePath = args[0];

			log.info("Starting of AS400_DATAMIGRATION . . .! " + LocalDateTime.now());
			as400DataMigrationServiceTest.process(filePath);
			log.info("Ending   of AS400_DATAMIGRATION . . .! " + LocalDateTime.now());

		} catch (Exception e) {

			e.printStackTrace();
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			log.trace(exceptionAsString);

			// emailsender.sendMail(exceptionAsString);
		}

	}

}
