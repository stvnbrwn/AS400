package com.as400datamigration.reposistory.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.as400datamigration.audit.AuditMessage;
import com.as400datamigration.audit.BatchDetailStatus;
import com.as400datamigration.audit.TableStatus;
import com.as400datamigration.common.Utility;
import com.as400datamigration.model.TableMetaData;
import com.as400datamigration.model.TableProcess;
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

	public void createTable(TableMetaData tableMetaData) {
		postgresTemplate.execute(tableMetaData.getPostgresQueries().getCreateTable());
	}

	@Override
	public void saveIntoTableProcess(Object[] tableProcess) {
		try {
			postgresTemplate.update(utility.getInsertIntoTableProcess(), tableProcess);
		} catch (Exception e) {
			log.error("Batch insert fail !!!", e);
		}

	}

	public void saveAllBatchDetail(String tableName, Long minRrn, Long maxRrn, LocalDateTime startedAt,
			BatchDetailStatus status, LocalDateTime endedAt, LocalDateTime modified) {

		try {
			// log.info("Batch insert start :-" + "batch size : " + tableDataList.size() +"
			// "+ LocalDateTime.now());
			// postgresTemplate.update(utility.getAllBatchDeatil(tableName), tableProcess);
			// log.info("Batch insert end :-" + "batch size : " + tableDataList.size() +" "+
			// LocalDateTime.now());
		} catch (Exception e) {
			log.error("Batch insert fail !!!");
			e.printStackTrace();
		}

	}

	@Override
	@Transactional
	public boolean writeOpraionOnTable(TableMetaData tableMetaData, List<Object[]> tableDataList) {

		boolean insertbatch = false;
		try {
			log.info("Batch insert start :-" + "batch size : " + tableDataList.size() + " " + LocalDateTime.now());

			tableMetaData.getBatchDetail().setStatus(BatchDetailStatus.Started_At_Destination);
			tableMetaData.getBatchDetail().setStartedAtDestination(LocalDateTime.now());
			tableMetaData.getBatchDetail().setModifiedAt(LocalDateTime.now());
			updateBatchDetail(tableMetaData.getBatchDetail().getUpdateObjArray());

			postgresTemplate.batchUpdate(tableMetaData.getPostgresQueries().getInsertTable(), tableDataList);

			tableMetaData.getBatchDetail().setStatus(BatchDetailStatus.Ended_At_Destination);
			tableMetaData.getBatchDetail().setEndedAtDestination(LocalDateTime.now());
			tableMetaData.getBatchDetail().setModifiedAt(LocalDateTime.now());
			updateBatchDetail(tableMetaData.getBatchDetail().getUpdateObjArray());
			insertbatch = true;
			log.info("Batch insert end   :-" + "batch size : " + tableDataList.size() + " " + LocalDateTime.now());
		} catch (Exception e) {
			log.error("Batch insert fail !!!", e);
			tableMetaData.getBatchDetail().setStatus(BatchDetailStatus.Failed_At_Destination);
			tableMetaData.getBatchDetail().setEndedAtDestination(LocalDateTime.now());
			tableMetaData.getBatchDetail().setModifiedAt(LocalDateTime.now());
			tableMetaData.getBatchDetail().setReason(AuditMessage.Execption_Msg + e);
			updateBatchDetail(tableMetaData.getBatchDetail().getUpdateObjArray()); // pending

			// pending
			updateTableProcessStatus(
					new TableProcess(tableMetaData.getTableName(), TableStatus.Table_Created_With_FailedBatch)
							.getUpdateObjArray());

		}
		return insertbatch;
	}

	@Override
	public void saveBatchDetail(Object[] allBatchDetails) {
		try {
			postgresTemplate.update(utility.getInsertIntoBatchDetail(), allBatchDetails);
		} catch (Exception e) {
			log.error(AuditMessage.Execption_Msg + "saveBatchDetail", e);
		}

	}

	@Override
	public void updateBatchDetail(Object[] allBatchDetails) {
		try {
			postgresTemplate.update(utility.getUpdateBatchDetail(), allBatchDetails);
		} catch (Exception e) {
			log.error(AuditMessage.Execption_Msg + "updateBatchDetail", e);
		}

	}

	@Override
	public void updateTableProcessStatus(Object[] allTableProcess) {
		try {
			postgresTemplate.update(utility.getUpdateTableProcess(), allTableProcess);
		} catch (Exception e) {
			log.error(AuditMessage.Execption_Msg + "updateTableProcessStatus", e);
		}

	}

}
