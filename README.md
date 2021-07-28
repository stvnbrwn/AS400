
# AS400 CSV Extract (Non-Java)

A utility is written on the AS400 that extracts all the identified 137 tables (most used) and saves it in .csv format in an IFS (AS400 Integrated File System) folder.
The Release document folder contains 2 files explaining this.
1. Detail information about the AS400 Tables and fields is provided in file
- Table_Metadata_Final.xlsx
2. The steps on how to run the CSV extract is explained in the below Word document.
- Tylie AS400 CSV Extract (Non-Java).docx



# ADM

AS400_Datamigration,
 it is spring boot application for data migration from as400(db2) to PostgreSQL
 
 ## PREREQUISITE
 	1. your system should have java 8 installed.
		it is require to run our java code.
	2. Maven tool should be install
		maven is building tool which help us to building our application. 
		we will use "mvn clean install" command to build our application.
		
		for details about maven please use the below link ...
		
https://maven.apache.org/what-is-maven.html
 
 ## INSTRUCTIONS
  						
						*** INSTRUCTIONS ***
	1. Run SQL Script File. (Name: ADM.SQL)
	
	#NOTE: if you want to change schema name at destination, then open SQL script file and change schema. (**Not Preferable**) 
	
	#Example: - 
		Old config: CREATE SCHEMA IF NOT EXISTS ADM_AUDIT;
		New config: CREATE SCHEMA IF NOT EXISTS <YOUR_AUDIT_SCHEMA_NAME>;
		Old config: CREATE SCHEMA IF NOT EXISTS ADM;
		New config: CREATE SCHEMA IF NOT EXISTS <YOUR_DESTINATION_SCHEMA_NAME>;
		
	2. Project Structure. (preferable project Structure)
	
![image](https://user-images.githubusercontent.com/75680603/126644337-f8dea28c-ad89-4eef-9b61-b1d790dcbbb0.png)

	3. Set Application Configuration.	
		a) Logging Configuration: Log file contains the detailed description of what is going in the application.
		If any issue is encountered in future, then it can be analysed by checking the logs.
		Below are some configurations we have in the configuration file for proper logging.
			*	logging.file= This contains the path where log file will be generated.
			*	logging.level.org.springframework=INFO
			*	logging.level.com.as400DataMigration=INFO
			*	logging.file.max-size= This keeps the maximum size of each log file.
			 	A new log file will be rolled out if size of the current log file exceeds this number.
			*	logging.file.total-size-cap= You can control the total size of all log files under a specified number.
			*	logging.file.max-history= You can specify the maximum number of days that the archive log files are kept.
		b) Database Configuration: 
			#AS400 DB config
			*   as400.datasource.jdbc-url= This refers to the url of the source server used.
			#Example: - 
				"jdbc:as400://129.40.95.145;translate binary=true;ccsid=37;"
			here, we are setting translate property with source server. (required for As400)
			*   as400.datasource.username= provide the user name of source server you want to use.
			*   as400.datasource.password =  provide the password of source server.
			*   as400.datasource.driverClassName = provide the driver  name. 
			#Example: - 
				"com.ibm.as400.access.AS400JDBCDriver"
			*   as400.datasource.hikari.connection-test-query= this will test connection with source server. 
			#Example: - 
				"values 1"  
			*   as400.datasource.hikari.validationTimeout= Provide connection Time out  
			#Example: - 
				"3000000" ( 50 * 60 * 1000 = 50 min )
			#Postgres DB config
			*   postgres.datasource.jdbc-url= This refers to the url of the destination server used.
			#Example: - 
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
			Note: Here these two properties will be set according to ADM.sql script.
		b) Thread Configuration: pool.size= "5" ,it defines thread pool size. 
		c) Batch configuration: batch.size= "2" ,  it defines size of the batch, which will be processed in one go.
    		
	#Command to run with default configuration:-
    		java -jar -Dspring.config.location=<Directory location>\application.properties 
    			  <Directory location>\ADM-v1.0.jar
   	#Example: -
    		"java -jar -Dspring.config.location=E:\ADM\configuration\application.properties 
    			  E:\ADM\ADM-v1.0.jar"
    			     
   	##Imp Note 
   		we can use "CONSOLE - Cheat Sheet" from "Command sheet.xlsx" which is available in 
		Release Documents Folder which will create cmd command for you. 
	If you have already done all the set-up let's continue...
	
## How to create jar file
	once you clone this git project on your local,
	open terminal/cmd with project folder location and use "mvn clean install" command to create jar.
	
![image](https://user-images.githubusercontent.com/75680603/127272043-242288ac-27f3-48bd-aac4-ed6b426dc331.png)
	
	in above picture we are showing my project location
	
![Untitled](https://user-images.githubusercontent.com/75680603/127298710-99368e6c-19c6-4077-a133-45400f1e7e8a.png)
	
	Run "mvn clean install" commnd at there, when it finished it will automatically create jar file in target folder.
	
![image](https://user-images.githubusercontent.com/75680603/127272226-c950bf15-b5af-4103-b47f-03d84762a7db.png)

	in above picture we are showing jar file in target folder. which resides in project folder it self.

    
## Application module
	this application has 2 modules
	1) Console Module
		for console module we can use "CONSOLE - Cheat Sheet" from "Command sheet.xlsx" which is available in 
		Release Documents Folder. it will create cmd command for you.
	2) Cron Module
		for cron module we can use "CRON - Cheat Sheet" from "Command sheet.xlsx" which is available in 
		Release Documents Folder. it will create cmd command for various application options.
    
## Options available in both module

	1) For Console Module 
![image](https://user-images.githubusercontent.com/75680603/126996396-d147badf-d0a5-4095-b159-4b904f722d77.png)

	2) For Cron Module 
		For those who want to use this utility in cron scheduler for them the below 5 options are available. 
![image](https://user-images.githubusercontent.com/75680603/126996884-934b9c6b-f067-4003-bbd4-7b38134d09bd.png)

## Input File Example

	you can create a simple txt file which contains all table name with library name for migration.		
![image](https://user-images.githubusercontent.com/75680603/126998148-ad7f934e-a65d-4f3a-8763-e26c7308a02f.png)	

	so here NIK is library name and after dot "." we have table name, we can have any number of input tables in input file. 
	
## How to use Console Module
	for console module we can use "CONSOLE - Cheat Sheet" from "Command sheet.xlsx" which is available in Release Documents Folder,
	it will create cmd command for you. 
	
	Here we are explained how to use that sheet.
	
![image](https://user-images.githubusercontent.com/75680603/127004942-58e65660-78cb-4ea0-864c-4975a3a03fef.png)
	
	so, in that sheet we have 2 input columns which is in green color. 
		1) Jar path: put complete jar path with jar name. 
			#Example: C:/Users/MohitKachhwaha/Desktop/adm_client/ADM-v1.0.jar
		2) Configuration File path: put configuration file path with jar name.
			#Example: C:/Users/MohitKachhwaha/Desktop/adm_client/configuration/dev.properties
	
	Console command will automatically get created in command column. 
    
## How to use Cron Module
	for cron module we can use "CRON - Cheat Sheet" from "Command sheet.xlsx" which is available in Release Documents Folder, 
	it will create cmd command for you. 
	
	Here we are explain how to use that sheet.
	
![image](https://user-images.githubusercontent.com/75680603/126994041-ccc7ab71-cc89-4b95-8f17-cbda2a28972b.png)

	so, in that sheet we have three input columns which is in green color. 
		1) Jar path: put complete jar path with jar name. 
			#Example: C:/Users/MohitKachhwaha/Desktop/adm_client/ADM-v1.0.jar
		2) Configuration File path: put configuration file path with jar name.
			#Example: C:/Users/MohitKachhwaha/Desktop/adm_client/configuration/dev.properties
		3) Input File Path: put your text file path which contain your input tables.
			#Example: C:\Users\MohitKachhwaha\Desktop\adm_client\inputFile.txt
			##Note: we dont need input File path for help option.
			
	cron command will automatically created in command column for specific option as show in screenshot.  
    
## How to check source and destination connection
	
	#If both connections established 
![image](https://user-images.githubusercontent.com/75680603/126661325-f7855d8f-ed4b-4aa9-8d70-7a7e8824cbfc.png)
	
	#If source is not connected 
![image](https://user-images.githubusercontent.com/75680603/126661445-1772e8d4-5c8d-4cff-bac4-19e74b513f12.png)
	 in this case tables are also logged in destination log tables , so can check table

	#If destination is not connected
![image](https://user-images.githubusercontent.com/75680603/126661560-ae34aae5-3bfa-43f8-a614-f01e55323c93.png)


    

 

 
                                      


