-- ADM is schema name
-- You can use any name
-- according to it please change below query/queries 

CREATE SCHEMA IF NOT EXISTS ADM;

CREATE TABLE IF NOT EXISTS ADM.all_betch_details
(   bno SERIAL PRIMARY KEY, 
    table_name VARCHAR,
    starting_rrn NUMERIC,
    ending_rrn NUMERIC,
    started_at TIMESTAMP,
    status	VARCHAR,	 	
    ended_at	TIMESTAMP,	
    modified_at	TIMESTAMP 
); 

CREATE TABLE IF NOT EXISTS ADM.failed_betch_details(
    fbno serial PRIMARY KEY,
    bno INT,
    table_name VARCHAR,
    starting_rrn NUMERIC,  
    ending_rrn NUMERIC,
    started_at TIMESTAMP,
    attempts int,  
    ended_at	TIMESTAMP,	
    modified_at	TIMESTAMP,
    CONSTRAINT fkey FOREIGN KEY (bno)
        REFERENCES adm.betch_details (bno)
);

