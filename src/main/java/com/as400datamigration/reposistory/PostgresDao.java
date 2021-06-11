package com.as400datamigration.reposistory;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.as400datamigration.common.AllBatchDetailStatus;
import com.as400datamigration.model.SQLColumn;
import com.as400datamigration.model.TableMetaData;


@Repository
public interface PostgresDao {

	
	public void createTable(String crtQuery, TableMetaData tableMetaData) ;

	public void insertBatchInTable(String insertQuery, List<Object[]> tableDataList) ;

	void insertIntoAllTableProcess(Object[] tableProcess);

	/*
	 * public void saveAllBatchDetail(String tableName, Long minRrn, Long maxRrn,
	 * LocalDateTime startedAt, AllBatchDetailStatus status, LocalDateTime endedAt,
	 * LocalDateTime modified);
	 */
	
	public void saveAllBatchDetail(Object... args);


}
