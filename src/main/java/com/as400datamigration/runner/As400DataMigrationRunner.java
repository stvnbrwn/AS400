package com.as400datamigration.runner;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.as400datamigration.service.As400DataMigrationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Profile("Full")
public class As400DataMigrationRunner implements CommandLineRunner {

	@Autowired
	As400DataMigrationService as400DataMigrationService;

	@Override
	public void run(String... args) throws Exception {
		try {
			String filePath = null;
			if (args.length > 0)
				filePath = args[0];

			log.info("Starting of AS400_DATAMIGRATION . . .! " + LocalDateTime.now());
			as400DataMigrationService.process(filePath);
			log.info("Ending   of AS400_DATAMIGRATION . . .! " + LocalDateTime.now());

		} catch (Exception e) {

			e.printStackTrace();
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			log.trace(exceptionAsString);

		}

	}

}
