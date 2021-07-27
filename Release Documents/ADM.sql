-- ADM_AUDIT is schema name
-- You can use any name
-- according to it please change below query/queries 

CREATE SCHEMA IF NOT EXISTS ADM_AUDIT;

create schema IF NOT EXISTS ADM;

CREATE TABLE IF NOT EXISTS ADM_AUDIT.all_table_process
(   
    table_name VARCHAR PRIMARY KEY, 
    total_rows NUMERIC,
    min_rrn NUMERIC,
    max_rrn NUMERIC,
    created_at TIMESTAMP,
    status	VARCHAR, 
    modified_at TIMESTAMP default now(),  
    column_json varchar
); 

-- trigger and funtion for all_table_process_modified_at
CREATE FUNCTION ADM_AUDIT.all_table_process_modified_at() RETURNS trigger AS $$
BEGIN
  NEW.modified_at := NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER
  sync_all_table_process_modified_at
BEFORE INSERT or UPDATE ON
  ADM_AUDIT.all_table_process
FOR EACH ROW EXECUTE PROCEDURE
  ADM_AUDIT.all_table_process_modified_at();

-- table all_table_process_details
create TABLE if not EXISTS ADM_AUDIT.all_table_process_details (
    tpd_no serial,
    table_name VARCHAR,
    reason	VARCHAR,
    create_at TIMESTAMP default now(),
    CONSTRAINT all_table_process_details_fkey FOREIGN KEY (table_name)
        REFERENCES ADM_AUDIT.all_table_process (table_name)
);

-- trigger and funtion for all_table_process_details_create_at
CREATE FUNCTION ADM_AUDIT.all_table_process_details_create_at() RETURNS trigger AS $$
BEGIN
  NEW.create_at := NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER
  sync_all_table_process_details_create_at
BEFORE INSERT or UPDATE ON
  ADM_AUDIT.all_table_process_details
FOR EACH ROW EXECUTE PROCEDURE
  ADM_AUDIT.all_table_process_details_create_at();



CREATE TABLE IF NOT EXISTS ADM_AUDIT.all_batch_details
(   bno SERIAL PRIMARY KEY, 
    table_name                 VARCHAR,
    starting_rrn               NUMERIC,
    ending_rrn                 NUMERIC,
    started_at_source          TIMESTAMP,
    started_at_destination     TIMESTAMP,
    status	                   VARCHAR,	
    ended_at_source	           TIMESTAMP,
    ended_at_destination	   TIMESTAMP,
    modified_at	               TIMESTAMP ,
    reason                     varchar,
    column_json                    varchar,
    CONSTRAINT all_batch_details_fkey FOREIGN KEY (table_name)
        REFERENCES ADM_AUDIT.all_table_process (table_name)
); 

CREATE INDEX batch_status 
ON ADM_AUDIT.all_batch_details(status);

CREATE TABLE IF NOT EXISTS ADM_AUDIT.failed_batch_details(
    fbno serial PRIMARY KEY,
    bno             INT,
    started_at      TIMESTAMP,
    status          varchar, 
    ended_at	    TIMESTAMP,	
    reason          varchar,
    CONSTRAINT fkey FOREIGN KEY (bno)
        REFERENCES ADM_AUDIT.all_batch_details (bno)
);



  