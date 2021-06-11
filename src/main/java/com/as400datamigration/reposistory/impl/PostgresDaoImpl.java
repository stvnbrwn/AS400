package com.as400datamigration.reposistory.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.as400datamigration.common.AuditMessage;
import com.as400datamigration.common.BatchDetailStatus;
import com.as400datamigration.common.Utility;
import com.as400datamigration.model.BatchDetail;
import com.as400datamigration.model.TableMetaData;
import com.as400datamigration.reposistory.PostgresDao;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PostgresDaoImpl implements PostgresDao {
	
	@Autowired
	Utility utility;

	@Autowired
	@Qualifier("PostgresJdbcTemplate")
	private JdbcTemplate postgresTemplate;

	public void createTable(String crtQuery, TableMetaData tableMetaData) {
		try {
			log.info("Table creation start :" + LocalDateTime.now());
			postgresTemplate.execute(crtQuery);
			saveIntoAllTableProcess(new Object[] {tableMetaData.getTableName(),tableMetaData.getTotalRows(),AuditMessage.TABLE_STATE_SUCCESS,
					AuditMessage.TABLE_STATE_SUCCESS_MESSAGE });
			log.info("Table creation end   :" + LocalDateTime.now());
		} catch (Exception e) {
			log.error("Table creation fail !!!");
			
			saveIntoAllTableProcess(new Object[] {tableMetaData.getTableName(),tableMetaData.getTotalRows(),AuditMessage.TABLE_STATE_FAILED,
					AuditMessage.TABLE_STATE_FAILED_MESSAGE_AT_CREATION });
			e.printStackTrace();
		}
	}

	public void saveBatchInTable(String insertQuery, List<Object[]> tableDataList,BatchDetail allBatchDetails) {

		try {
			log.info("Batch insert start :-" + "batch size : " + tableDataList.size() +" "+ LocalDateTime.now());
			postgresTemplate.batchUpdate(insertQuery, tableDataList);
			saveAllBatchDetail(allBatchDetails,BatchDetailStatus.COMPLETED);
			log.info("Batch insert end   :-" + "batch size : " + tableDataList.size() +" "+ LocalDateTime.now());
		} catch (Exception e) {
			log.error("Batch insert fail !!!",e);
			saveAllBatchDetail(allBatchDetails,BatchDetailStatus.FAILED);
		}
	}


	@Override
	public void saveIntoAllTableProcess(Object[] tableProcess) {
		try {
			//log.info("Batch insert start :-" + "batch size : " + tableDataList.size() +" "+ LocalDateTime.now());
			postgresTemplate.update(utility.getAllTableProcess(tableProcess[0]), tableProcess);
			//log.info("Batch insert end   :-" + "batch size : " + tableDataList.size() +" "+ LocalDateTime.now());
		} catch (Exception e) {
			log.error("Batch insert fail !!!",e);
		}
		
	}

	
	public void saveAllBatchDetail(String tableName, Long minRrn, Long maxRrn, LocalDateTime startedAt,
			BatchDetailStatus status, LocalDateTime endedAt, LocalDateTime modified) {
		
		try {
			//log.info("Batch insert start :-" + "batch size : " + tableDataList.size() +" "+ LocalDateTime.now());
			//postgresTemplate.update(utility.getAllBatchDeatil(tableName), tableProcess);
			//log.info("Batch insert end   :-" + "batch size : " + tableDataList.size() +" "+ LocalDateTime.now());
		} catch (Exception e) {
			log.error("Batch insert fail !!!");
			e.printStackTrace();
		}
		
	}

	@Override
	public void saveAllBatchDetail(BatchDetail batchDetail,BatchDetailStatus status) {
		try {
			
			//batchDetail.getObjArrayOnstatus(status);
			
			String InsertQry=utility.getAllBatchDetail(status);
			
			
			
			//log.info("Batch insert start :-" + "batch size : " + tableDataList.size() +" "+ LocalDateTime.now());
			postgresTemplate.update(utility.getAllBatchDeatil(batchDetail.getTableName()),batchDetail.getObjArray());
			//log.info("Batch insert end   :-" + "batch size : " + tableDataList.size() +" "+ LocalDateTime.now());
		} catch (Exception e) {
			log.error("Batch insert fail !!!",e);
		}
		
		
	}

}
