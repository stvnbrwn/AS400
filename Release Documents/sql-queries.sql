-- drop 

drop schema adm CASCADE;
drop schema adm_audit CASCADE;


SELECT *
FROM pg_catalog.pg_tables
WHERE schemaname != 'pg_catalog' AND 
    schemaname != 'adm_audit'
    and
    schemaname != 'information_schema';

select * from adm_audit.all_table_process;










