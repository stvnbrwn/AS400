--

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

SELECT  (
SELECT  COUNT(*)
FROM ADM.CPCUST ) AS table1_rows , (
SELECT  COUNT(*)
FROM ADM.DPF ) AS table2_rows , (
SELECT  COUNT(*)
FROM ADM.EMPL1 ) AS table3_rows , (
SELECT  COUNT(*)
FROM ADM.EMPL2 ) AS table4_rows , (
SELECT  COUNT(*)
FROM ADM.EMPL3 ) AS table5_rows , (
SELECT  COUNT(*)
FROM ADM.EMPPF ) AS table6_rows , (
SELECT  COUNT(*)
FROM ADM.EMPS ) AS table7_rows 


select count(*) as table1_rows ,min(rrn(a)),max(rrn(a))    from NIK.CPCUST a union all 
select count(*) as table1_rows ,min(rrn(a)),max(rrn(a))    from NIK.DPF   a union all 
select count(*) as table1_rows ,min(rrn(a)),max(rrn(a))   from NIK.EMPL1  a union all 
select count(*) as table1_rows ,min(rrn(a)),max(rrn(a))    from NIK.EMPL2   a union all 
select count(*) as table1_rows ,min(rrn(a)),max(rrn(a))    from NIK.EMPL3   a union all 
select count(*) as table1_rows ,min(rrn(a)),max(rrn(a))    from NIK.EMPPF a  union all 
select count(*) as table1_rows ,min(rrn(a)),max(rrn(a))    from NIK.EMPS a;
  

SELECT rrn(a) as rrn, a.* FROM NIK.CPCUST a where rrn(a) between 19 and 20;

insert into NIK.CPCUST ( CNUMBER ,
CNAME  ,
EMAIL  ) values ( 254 , 'test1fn' , 'test@123.com'); insert into NIK.DPF   ( EMPID ,
NAME  ,
EMAIL ,
NUMBER,
AGE    ) values ( 254 , 'test1fn' , 'test@123.com', 4445557771, 23) ;insert into NIK.EMPL1  ( NAME  , 
SALARY  ) values (  'test1fn' , 5555); 
