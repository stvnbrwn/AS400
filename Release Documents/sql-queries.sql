DROP schema adm CASCADE;

DROP schema adm_audit CASCADE;

DROP FUNCTION IF EXISTS all_table_process_modified_at CASCADE ;

DROP FUNCTION IF EXISTS all_table_process_details_create_at CASCADE;

SELECT  *
FROM pg_catalog.pg_tables
WHERE schemaname != 'pg_catalog' 
AND schemaname != 'adm_audit' 
AND schemaname != 'information_schema'; 

SELECT  *
FROM adm_audit.all_table_process;

