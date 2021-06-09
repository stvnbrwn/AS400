package com.as400datamigration.reposistory;

import java.util.List;

import org.springframework.stereotype.Repository;


@Repository
public interface PostgresDao {

	
	public void createTable(String crtQuery) ;

	public void insertBatchInTable(String insertQuery, List<Object[]> tableDataList) ;

}
