package com.as400datamigration.reposistory;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.as400datamigration.common.BatchDetailStatus;
import com.as400datamigration.model.BatchDetail;
import com.as400datamigration.model.SQLColumn;
import com.as400datamigration.model.TableMetaData;


@Repository
public interface PostgresDao {

	
	public void createTable(String crtQuery, TableMetaData tableMetaData) ;

	public void saveBatchInTable(String insertQuery, List<Object[]> tableDataList, BatchDetail allBatchDetails) ;

	void saveIntoAllTableProcess(Object[] tableProcess);

	/*
	 * public void saveAllBatchDetail(String tableName, Long minRrn, Long maxRrn,
	 * LocalDateTime startedAt, AllBatchDetailStatus status, LocalDateTime endedAt,
	 * LocalDateTime modified);
	 */
	
	public void saveAllBatchDetail(BatchDetail allBatchDetails,BatchDetailStatus status);


}
