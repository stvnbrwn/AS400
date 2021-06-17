package com.as400datamigration.reposistory.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.as400datamigration.audit.AuditMessage;
import com.as400datamigration.audit.BatchDetailStatus;
import com.as400datamigration.audit.FailBatchStatus;
import com.as400datamigration.common.Utility;
import com.as400datamigration.model.BatchDetail;
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
	
	@Override
	public void updateTableProcessStatus(Object[] allTableProcess) {
		try {
			postgresTemplate.update(utility.getUpdateTableProcessStatus(), allTableProcess);
		} catch (Exception e) {
			log.error(AuditMessage.Execption_Msg + "updateTableProcessStatus", e);
		}

	}
	
	/*
	 * @Override public void updateTableProcessMetaData(Object[] updateObjArray) {
	 * try { postgresTemplate.update(utility.getUpdateTableProcessMetaData(),
	 * updateObjArray); } catch (Exception e) { log.error(AuditMessage.Execption_Msg
	 * + "updateTableProcessStatus", e); } }
	 */

	/*
	 * public void saveAllBatchDetail(String tableName, Long minRrn, Long maxRrn,
	 * LocalDateTime startedAt, BatchDetailStatus status, LocalDateTime endedAt,
	 * LocalDateTime modified) {
	 * 
	 * try { // log.info("Batch insert start :-" + "batch size : " +
	 * tableDataList.size() +" // "+ LocalDateTime.now()); //
	 * postgresTemplate.update(utility.getAllBatchDeatil(tableName), tableProcess);
	 * // log.info("Batch insert end :-" + "batch size : " + tableDataList.size()
	 * +" "+ // LocalDateTime.now()); } catch (Exception e) {
	 * log.error("Batch insert fail !!!"); e.printStackTrace(); }
	 * 
	 * }
	 */

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
			tableMetaData.getBatchDetail().setColumnsJson(tableMetaData.getTableProcess().getColumnsJson());
			tableMetaData.getBatchDetail().setReason(AuditMessage.Execption_Msg + e);
			updateBatchDetail(tableMetaData.getBatchDetail().getUpdateObjArray()); // pending

			// pending
			/*
			 * updateTableProcessStatus( new TableProcess(tableMetaData.getTableName(),
			 * TableStatus.Table_Created_With_FailedBatch) .getUpdateObjArray());
			 */
		}
		return insertbatch;
	}

	@Override
	public long saveBatchDetail(Object[] allBatchDetails) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		try {
			//postgresTemplate.update();
			postgresTemplate.update(utility.getPrepareStatement(utility.getInsertIntoBatchDetail(), allBatchDetails, 
					new String[] {"bno"}), keyHolder);
		} catch (Exception e) {
			log.error(AuditMessage.Execption_Msg + "saveBatchDetail", e);
		}
		return keyHolder.getKey().longValue();
	}
	
	
	
	@Override
	public long saveBatchDetail_t(BatchDetail batchDetail) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		try {
			 SqlParameterSource data = new BeanPropertySqlParameterSource(batchDetail);
			postgresTemplate.update(utility.getInsertIntoBatchDetail(), data,keyHolder);
		} catch (Exception e) {
			log.error(AuditMessage.Execption_Msg + "saveBatchDetail", e);
		}
		return keyHolder.getKey().longValue();
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
	public List<BatchDetail> getfailedbatch() {
		List<BatchDetail> batchDetails=null;
		try {
			 batchDetails= postgresTemplate.query(utility.fetchFailedbatch(),
					new BeanPropertyRowMapper<BatchDetail>(BatchDetail.class));
			 //constructor
		} catch (Exception e) {
			log.error(AuditMessage.Execption_Msg + "updateBatchDetail", e);
		}
		return batchDetails;
	}

	@Override
	public long saveFailedBatchDetail(Object[] saveObjArray) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		try {
			postgresTemplate.update(utility.getInsertIntoFailedBatch(), saveObjArray, keyHolder);
		} catch (Exception e) {
			log.error(AuditMessage.Execption_Msg + "saveBatchDetail", e);
		}
		return keyHolder.getKey().longValue();
	}
	
	@Override
	public void updateFailedBatchDetail(Object[] UpdateObjArray) {
		try {
			postgresTemplate.update(utility.getUpdateFailedBatchDetail(), UpdateObjArray);
		} catch (Exception e) {
			log.error(AuditMessage.Execption_Msg + "updateBatchDetail", e);
		}

	}

	@Override
	public void writeOpraionFailedBatch(TableMetaData tableMetaData, List<Object[]> tableDataList) {
		try {
			log.info("Batch writeOpraionFailedBatch start :-" + "batch size : " + tableDataList.size() + " " + LocalDateTime.now());

			postgresTemplate.batchUpdate(tableMetaData.getPostgresQueries().getInsertTable(), tableDataList);

			tableMetaData.getFailedBatchDetails().setStatus(FailBatchStatus.Pass);
			tableMetaData.getFailedBatchDetails().setEndedAt(LocalDateTime.now());
			updateFailedBatchDetail(tableMetaData.getFailedBatchDetails().getUpdateObjArray());
			log.info("Batch writeOpraionFailedBatch end   :-" + "batch size : " + tableDataList.size() + " " + LocalDateTime.now());
		} catch (Exception e) {
			log.error("Batch insert fail !!!", e);
			tableMetaData.getFailedBatchDetails().setStatus(FailBatchStatus.Fail);
			tableMetaData.getFailedBatchDetails().setEndedAt(LocalDateTime.now());
			tableMetaData.getFailedBatchDetails().setReason(AuditMessage.Execption_Msg + e);
			updateFailedBatchDetail(tableMetaData.getFailedBatchDetails().getUpdateObjArray());

			// pending
			/*
			 * updateTableProcessStatus( new TableProcess(tableMetaData.getTableName(),
			 * TableStatus.Table_Created_With_FailedBatch) .getUpdateObjArray());
			 */
		}
	}

	@Override
	public TableProcess getTableMetaDataFromDestination(TableMetaData tableMetaData) {
		TableProcess tableProcess=null;
		try {
			tableProcess = (TableProcess) postgresTemplate.queryForObject(
					utility.getTableProcessMetaData(tableMetaData.getTableName()),
					new BeanPropertyRowMapper<TableProcess>(TableProcess.class));
		} catch (Exception e) {
			// table not at - > destination
			// or other 
			throw e;
		}
		return tableProcess;
	}

}
