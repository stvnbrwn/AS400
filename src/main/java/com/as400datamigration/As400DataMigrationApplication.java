package com.as400datamigration;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class As400DataMigrationApplication {

	public static void main(String[] args) {
		
		new SpringApplicationBuilder(As400DataMigrationApplication.class).logStartupInfo(false).run(args);
 
	}

}
