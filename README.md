# ADM

AS400_Datamigration,
 it is spring boot console application for data migration from as400(db2) to postgresql
 
 ## INSTRUCTIONS
  						
						*** INSTRUCTIONS ***
	1. Run SQL Script File. (Name : ADM.SQL)
	
	#NOTE : if you want to change schema name at destination, then open SQL script file and change schema. (**Not Preferable**) 
	
	#Example : - 
		Old config : CREATE SCHEMA IF NOT EXISTS ADM_AUDIT;
		New config : CREATE SCHEMA IF NOT EXISTS <YOUR_AUDIT_SCHEMA_NAME>;
		Old config : CREATE SCHEMA IF NOT EXISTS ADM;
		New config : CREATE SCHEMA IF NOT EXISTS <YOUR_DESTINATION_SCHEMA_NAME>;
		
	2. Project Structure. ( preferable project Structure )
	
![image](https://user-images.githubusercontent.com/75680603/126644337-f8dea28c-ad89-4eef-9b61-b1d790dcbbb0.png)

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
    
## Options available in utility

	1) For console utility 
![image](https://user-images.githubusercontent.com/75680603/126996396-d147badf-d0a5-4095-b159-4b904f722d77.png)

	2) For cron uses 
		those who wanted to use this utility in cron scheduler for that there is only these 5 option.
![image](https://user-images.githubusercontent.com/75680603/126996884-934b9c6b-f067-4003-bbd4-7b38134d09bd.png)

## Input File Example

	you can create a simple txt file which conatain all table name with library name for migration.		
![image](https://user-images.githubusercontent.com/75680603/126998148-ad7f934e-a65d-4f3a-8763-e26c7308a02f.png)	

	so here NIK is library name and after dot "." we have table name. we can any number of input tables in input file.
    
## How to use this utility in cron scheduler 
	we create "Command sheet.xlsx" file for creating cron scheduler commands for using different application options.
	
	Here we are explaing how to use that sheet.
	
![image](https://user-images.githubusercontent.com/75680603/126994041-ccc7ab71-cc89-4b95-8f17-cbda2a28972b.png)

	so in that sheet we have three input columns which is in green color. 
		1) Jar path : put complete jar path with jar name. 
			#Example : C:/Users/MohitKachhwaha/Desktop/adm_client/ADM-v1.0.jar
		2) Configuration File path : put configuration file path with jar name.
			#Example : C:/Users/MohitKachhwaha/Desktop/adm_client/configuration/dev.properties
		3) Input File Path : put your text file path which contain your input tables.
			#Example : C:\Users\MohitKachhwaha\Desktop\adm_client\inputFile.txt
			##Note : we dont need input File path for help option.
			
	cron command will automaically created in command column for specific option as show in screenshot. 
    
## How to check source and destination connection
	
	#If both connection established 
![image](https://user-images.githubusercontent.com/75680603/126661325-f7855d8f-ed4b-4aa9-8d70-7a7e8824cbfc.png)
	
	#If source is not connected 
![image](https://user-images.githubusercontent.com/75680603/126661445-1772e8d4-5c8d-4cff-bac4-19e74b513f12.png)
	 in this case tables are also logged in destination log tables , so can check table

	#If destination is not connected
![image](https://user-images.githubusercontent.com/75680603/126661560-ae34aae5-3bfa-43f8-a614-f01e55323c93.png)


    

 

 
                                      


