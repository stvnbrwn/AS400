-- ADM is schema name
-- You can use any name
-- according to it please change below query/queries 

CREATE SCHEMA IF NOT EXISTS ADM;

--create schema if not exists D_schema;

CREATE TABLE IF NOT EXISTS ADM.all_table_process
(   
    table_name VARCHAR PRIMARY KEY, 
    total_rows NUMERIC,
    max_rrn NUMERIC,
    create_timestamp TIMESTAMP,
    status	VARCHAR, 
    -- Table_Not_Found_At_Source,
	-- Table_Desc_Not_Found_At_Source,
	-- Table_Creation_Failed,	
	-- Table_Created_With_NO_Data,
	-- Table_Created_And_InProcess, -->update
	-- Table_Created_And_AllBatchCompleted
    -- sync failed at source
    -- sync failed at destination
    --modified_timestamp TIMESTAMP,
    reason	VARCHAR,	
    columns varchar
); 

create TABLE if not EXISTS ADM.all_table_process_details (
    tpd_no serial,
    table_name VARCHAR,
    reason	VARCHAR,
    create_timestamp TIMESTAMP
);


CREATE TABLE IF NOT EXISTS ADM.all_batch_details
(   bno SERIAL PRIMARY KEY, 
    table_name                 VARCHAR,
    starting_rrn               NUMERIC,
    ending_rrn                 NUMERIC,
    started_at_source          TIMESTAMP,
    started_at_destination     TIMESTAMP,
    
    status	                   VARCHAR,	
                                 -- Started_At_Source,	Failed_At_Source,	Ended_At_Source,
	                             -- Started_At_Destination,	Failed_At_Destination,	Ended_At_Destination,
	                             -- Batch_Refactored , Max_Attemp_
    ended_at_source	           TIMESTAMP,
    ended_at_destination	   TIMESTAMP,
    
    modified_at	               TIMESTAMP ,
    reason                     varchar,
    columnsJson                    varchar,
   -- INDEX status (status),
    CONSTRAINT all_batch_details_fkey FOREIGN KEY (table_name)
        REFERENCES adm.all_table_process (table_name)
); 

CREATE INDEX batch_status 
ON ADM.all_batch_details(status);

CREATE TABLE IF NOT EXISTS ADM.failed_batch_details(
    fbno serial PRIMARY KEY,
    bno             INT,
    started_at      TIMESTAMP,
    status          varchar, 
    ended_at	    TIMESTAMP,	
    reason          varchar,
    CONSTRAINT fkey FOREIGN KEY (bno)
        REFERENCES adm.all_batch_details (bno)
);


-- SELECT  bno
--        ,table_name 
--        ,starting_rrn 
--        ,ending_rrn 
--        ,status
-- FROM all_betch_details 
-- WHERE status = 'Failed_At_Source' or status = 'Failed_At_Destination' ; 


  