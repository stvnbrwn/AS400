# ADM

AS400_Datamigration,
 it is spring boot console application for data migration from as400(db2) to postgresql
 
                                      ***  INSTRUCTIONS  ***

	1. Run SQL Script File. (Name : ADM.SQL)
	#NOTE : if you want to change schema name at destination, then open SQL script file and change schema. (**Not Preferable**)
	
	#Example : - 
		Old config : CREATE SCHEMA IF NOT EXISTS ADM_AUDIT;
		New config : CREATE SCHEMA IF NOT EXISTS <YOUR_AUDIT_SCHEMA_NAME>;

		Old config : CREATE SCHEMA IF NOT EXISTS ADM;
		New config : CREATE SCHEMA IF NOT EXISTS <YOUR_DESTINATION_SCHEMA_NAME>;
	

	2. Project Structure. ( preferable project Structure ) 
	
				![image](https://user-images.githubusercontent.com/75680603/126644227-9ed12c3d-0886-482e-9e05-9fa39958a2ef.png)

  				      
	3. Set Application Configuration.
	
		a) Logging Configuration : Log file contains the detailed description of what is going in the application.
		If any issue is encountered in future then it can be analyzed by checking the logs.
		Below are some configurations we have in the configuration file for proper logging.
			*	logging.file= This contains the path where log file will be generated.
			*	logging.level.org.springframework=INFO
			*	logging.level.com.as400DataMigration=INFO
			*	logging.file.max-size= This keeps the maximum size of each log file.
			 	A new log file will be rolled out if size of the current log file exceeds this number.
			*	logging.file.total-size-cap= You can control the total size of all log files under a specified number.
			*	logging.file.max-history= You can specify the maximum number of days that the archive log files are kept.
   		 
		b) Database Configuration : 
			#AS400 DB config
			*   as400.datasource.jdbc-url= This refers to the url of the source server used.
			#Example : - 
				"jdbc:as400://129.40.95.145;translate binary=true;ccsid=37;"
			here, we are setting translate property with source server. (required for As400)

			*   as400.datasource.username= provide the user name of source server you want to use.
			*   as400.datasource.password =  provide the password of source server.
			*   as400.datasource.driverClassName = provide the driver  name. 
			#Example : - 
				"com.ibm.as400.access.AS400JDBCDriver"

			*   as400.datasource.hikari.connection-test-query= this will test connection with source server. 
			#Example : - 
				"values 1"  
			*   as400.datasource.hikari.validationTimeout= Provide connection Time out  
			#Example : - 
				"3000000" ( 50 * 60 * 1000 = 50 min )

			#postgres DB config

			*   postgres.datasource.jdbc-url= This refers to the url of the destination server used.
			#Example : - 
				"jdbc:postgresql://localhost:5434/postgres?useSSL=false"
				here, we are setting SSL property with destination server. 

			*   postgres.datasource.username= provide the user name of destination server you want to use.
			*   postgres.datasource.password =  provide the password of destination server.
			*   postgres.datasource.driverClassName = provide the driver  name. 
			#Example : - 
				"org.postgresql.Driver"

	4. Other properties
		a) Following are the two different schema name properties: 
			postgres.schema=ADM
			postgres.audit.schema=ADM_AUDIT
			Note : Here these two properties will be set according to ADM.sql script.

		b) Thread Configuration : pool.size= "5" ,its define thread pool size 
		c) Batch configuration : batch.size= "2" , its define size of batch, which will processed in one time.
		
		                                    
    #Command to run with default configuration :-
    	java -jar -Dspring.config.location=<Directory location>\application.properties 
    			  <Directory location>\ADM-v1.0.jar
    #Example : -
    		"java -jar -Dspring.config.location=E:\ADM\configuration\application.properties 
    			  E:\ADM\ADM-v1.0.jar"
    			  
    #Imp Note :- you can use "Command sheet.xlsx" from Release Documents Folder. which will create cmd command for you.
	

if you already did these set-up let's continue...


