-- ADM is schema name
-- You can use any name
-- according to it please change below query/queries 

CREATE SCHEMA IF NOT EXISTS ADM;

CREATE TABLE IF NOT EXISTS ADM.all_table_process
(   
    table_name VARCHAR PRIMARY KEY, 
    total_rows NUMERIC,
    max_rrn NUMERIC,
    status	VARCHAR, 
    -- Table_Not_Found,
	-- Table_Created_With_NO_Data,
	-- Table_Created_And_InRunning,
	-- Table_Created_And_AllBatchCompleted
    reason	VARCHAR	
); 


CREATE TABLE IF NOT EXISTS ADM.all_betch_details
(   bno SERIAL PRIMARY KEY, 
    table_name                 VARCHAR,
    starting_rrn               NUMERIC,
    ending_rrn                 NUMERIC,
    started_at_source          TIMESTAMP,
    started_at_destination     TIMESTAMP,
    
    status	                   VARCHAR,	
                                 -- Started_At_Source,	Failed_At_Source,	Ended_At_Source,
	                             -- Started_At_Destination,	Failed_At_Destination,	Ended_At_Destination,
	                             -- Batch_Refactored
    ended_at_source	           TIMESTAMP,
    ended_at_destination	   TIMESTAMP,
    
    modified_at	               TIMESTAMP ,
    reason                     varchar,
    INDEX status_index (status),
    CONSTRAINT all_betch_details_fkey FOREIGN KEY (table_name)
        REFERENCES adm.all_table_process (table_name)
); 

CREATE TABLE IF NOT EXISTS ADM.failed_betch_details(
    fbno serial PRIMARY KEY,
    bno             INT,
    started_at      TIMESTAMP,
    status          varchar, 
    ended_at	    TIMESTAMP,	
    reason          varchar,
    CONSTRAINT fkey FOREIGN KEY (bno)
        REFERENCES adm.all_betch_details (bno)
);


SELECT  a.table_name 
       ,starting_rrn 
       ,ending_rrn 
       ,started_at_source 
       ,started_at_destination
       ,a.status 
       ,ended_at_source 
       ,ended_at_destination 
       ,modified_at 
       ,b.reason
FROM all_table_process a
JOIN all_betch_details b
ON a.table_name = b.table_name
WHERE b.status = 'Failed_At_Source' or b.status = 'Failed_At_Destination' ;


  