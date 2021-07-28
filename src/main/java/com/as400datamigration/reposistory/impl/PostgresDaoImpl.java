package com.as400datamigration.reposistory.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.as400datamigration.audit.AuditMessage;
import com.as400datamigration.audit.BatchDetailStatus;
import com.as400datamigration.audit.FailBatchStatus;
import com.as400datamigration.common.Utility;
import com.as400datamigration.model.BatchDetail;
import com.as400datamigration.model.TableMetaData;
import com.as400datamigration.model.TableProcess;
import com.as400datamigration.model.AllTableRows;
import com.as400datamigration.reposistory.PostgresDao;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PostgresDaoImpl implements PostgresDao {

	@Autowired
	Utility utility;

	@Autowired
	@Qualifier("PostgresJdbcTemplate")
	private JdbcTemplate postgresTemplate;

	public void createTable(TableMetaData tableMetaData) {
		postgresTemplate.execute(tableMetaData.getPostgresQueries().getCreateTable());
	}
	//log table entry
	@Override
	public void saveIntoTableProcess(Object[] tableProcess) {
		postgresTemplate.update(utility.getInsertIntoTableProcess(), tableProcess);
	}

	@Override
	public void updateTableProcessStatus(Object[] allTableProcess) {
		try {
			postgresTemplate.update(utility.getUpdateTableProcessStatus(), allTableProcess);
		} catch (Exception e) {
			log.error(AuditMessage.EXECPTION_MSG + "updateTableProcessStatus", e);
		}

	}

	@Override
	@Transactional
	public boolean writeOpraionOnTable(TableMetaData tableMetaData, List<Object[]> tableDataList) {

		boolean insertbatch = false;
		try {
			log.info("Batch insert start :-" + "batch size : " + tableDataList.size() + " " + LocalDateTime.now());

			tableMetaData.getBatchDetail().setStatus(BatchDetailStatus.STARTED_AT_DESTINATION);
			tableMetaData.getBatchDetail().setStartedAtDestination(LocalDateTime.now());
			tableMetaData.getBatchDetail().setModifiedAt(LocalDateTime.now());
			updateBatchDetail(tableMetaData.getBatchDetail().getUpdateObjArray());

			if (Objects.isNull(tableMetaData.getPostgresQueries())
					|| Objects.isNull(tableMetaData.getPostgresQueries().getInsertTable()))
				tableMetaData.setPostgresQueries(utility.getPostgresQueries(tableMetaData));

			postgresTemplate.batchUpdate(tableMetaData.getPostgresQueries().getInsertTable(), tableDataList);

			tableMetaData.getBatchDetail().setStatus(BatchDetailStatus.ENDED_AT_DESTINATION);
			tableMetaData.getBatchDetail().setEndedAtDestination(LocalDateTime.now());
			tableMetaData.getBatchDetail().setModifiedAt(LocalDateTime.now());
			updateBatchDetail(tableMetaData.getBatchDetail().getUpdateObjArray());
			insertbatch = true;
			log.info("Batch insert end   :-" + "batch size : " + tableDataList.size() + " " + LocalDateTime.now());
		} catch (Exception e) {
			log.error("Batch insert fail !!!", e);
			tableMetaData.getBatchDetail().setStatus(BatchDetailStatus.FAILED_AT_DESTINATION);
			tableMetaData.getBatchDetail().setEndedAtDestination(LocalDateTime.now());
			tableMetaData.getBatchDetail().setModifiedAt(LocalDateTime.now());
			tableMetaData.getBatchDetail().setColumnJson(tableMetaData.getTableProcess().getColumnsJson());
			tableMetaData.getBatchDetail().setReason(AuditMessage.EXECPTION_MSG + e);
			updateBatchDetail(tableMetaData.getBatchDetail().getUpdateObjArray()); // pending

		}
		return insertbatch;
	}

	@Override
	public long saveBatchDetail(Object[] allBatchDetails) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		try {
			postgresTemplate.update(utility.getPrepareStatement(utility.getInsertIntoBatchDetail(), allBatchDetails,
					new String[] { "bno" }), keyHolder);
		} catch (Exception e) {
			log.error(AuditMessage.EXECPTION_MSG + "saveBatchDetail", e);
		}
		return keyHolder.getKey().longValue();
	}

	@Override
	public void updateBatchDetail(Object[] allBatchDetails) {
		try {
			postgresTemplate.update(utility.getUpdateBatchDetail(), allBatchDetails);
		} catch (Exception e) {
			log.error(AuditMessage.EXECPTION_MSG + "updateBatchDetail", e);
		}
	}

	@Override
	public List<BatchDetail> getfailedbatch() {
		List<BatchDetail> batchDetails = null;
		try {
			batchDetails = postgresTemplate.query(utility.fetchFailedbatch(),
					new BeanPropertyRowMapper<BatchDetail>(BatchDetail.class));
		} catch (Exception e) {
			log.error(AuditMessage.EXECPTION_MSG + "updateBatchDetail", e);
		}
		return batchDetails;
	}

	@Override
	public long saveFailedBatchDetail(Object[] saveObjArray) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		try {
			postgresTemplate.update(utility.getPrepareStatement(utility.getInsertIntoFailedBatch(), saveObjArray,
					new String[] { "fbno" }), keyHolder);
		} catch (Exception e) {
			log.error(AuditMessage.EXECPTION_MSG + "saveBatchDetail", e);
		}
		return keyHolder.getKey().longValue();
	}

	@Override
	public void updateFailedBatchDetail(Object[] UpdateObjArray) {
		try {
			postgresTemplate.update(utility.getUpdateFailedBatchDetail(), UpdateObjArray);
		} catch (Exception e) {
			log.error(AuditMessage.EXECPTION_MSG + "updateBatchDetail", e);
		}

	}

	@Override
	@Transactional
	public boolean writeOpraionFailedBatch(TableMetaData tableMetaData, List<Object[]> tableDataList) {
		try {
			log.info("Batch writeOpraionFailedBatch start :-" + "batch size : " + tableDataList.size() + " "
					+ LocalDateTime.now());

			if (Objects.isNull(tableMetaData.getPostgresQueries())
					|| Objects.isNull(tableMetaData.getPostgresQueries().getInsertTable()))
				tableMetaData.setPostgresQueries(utility.getPostgresQueries(tableMetaData));

			postgresTemplate.batchUpdate(tableMetaData.getPostgresQueries().getInsertTable(), tableDataList);

			tableMetaData.getFailedBatchDetails().setStatus(FailBatchStatus.PASS);
			tableMetaData.getFailedBatchDetails().setEndedAt(LocalDateTime.now());
			updateFailedBatchDetail(tableMetaData.getFailedBatchDetails().getUpdateObjArray());
			log.info("Batch writeOpraionFailedBatch end   :-" + "batch size : " + tableDataList.size() + " "
					+ LocalDateTime.now());
			return true;
		} catch (Exception e) {
			log.error("Batch insert fail !!!", e);
			tableMetaData.getFailedBatchDetails().setStatus(FailBatchStatus.FAIL);
			tableMetaData.getFailedBatchDetails().setEndedAt(LocalDateTime.now());
			tableMetaData.getFailedBatchDetails().setReason(AuditMessage.EXECPTION_MSG + e);
			updateFailedBatchDetail(tableMetaData.getFailedBatchDetails().getUpdateObjArray());
		}
		return false;
	}

	@Override
	public TableProcess getTableMetaData(String tableName) {
		TableProcess tableProcess = null;
		try {
			tableProcess = (TableProcess) postgresTemplate.queryForObject(utility.getTableProcessMetaData(tableName),
					new BeanPropertyRowMapper<TableProcess>(TableProcess.class));
		} catch (Exception e) {
			log.error("Connection error at Destination, or table not found " + tableName , e);
			throw e;
		}
		return tableProcess;
	}

	@Override
	public BatchDetail getlastBatchDetails(String tableName) {
		BatchDetail batchDetail = null;
		try {
			batchDetail = (BatchDetail) postgresTemplate.queryForObject(utility.getlastBatchDetails(tableName),
					new BeanPropertyRowMapper<BatchDetail>(BatchDetail.class));
		} catch (Exception e) {
			log.error("Connection error at Destination, or last batch details not found ", e);
		}

		return batchDetail;
	}

	@Override
	public void saveIntoTableProcessDetail(Object[] saveObjArray) {
		postgresTemplate.update(utility.getInsertIntoTableProcessDetail(), saveObjArray);
	}

	@Override
	public void updateTableDeatil(Object[] tableDetailsObjArray, boolean withCoulmns) {
		if (withCoulmns)
			postgresTemplate.update(utility.updateTableDeatil(), tableDetailsObjArray);
		else
			postgresTemplate.update(utility.updateTableDeatilWithoutCoulmns(), tableDetailsObjArray);
	}

	@Override
	public List<BatchDetail> getTenBatch(List<Integer> batchNoList) {
		List<BatchDetail> batchDetails = null;
		if (!batchNoList.isEmpty()) {
			batchDetails = postgresTemplate.query(utility.getAllBatchFromList(batchNoList),
					new BeanPropertyRowMapper<BatchDetail>(BatchDetail.class));
		} else {
			batchDetails = postgresTemplate.query(utility.getAllBatch(),
					new BeanPropertyRowMapper<BatchDetail>(BatchDetail.class));
		}

		return batchDetails;
	}

	@Override
	public void updateBatchDetailStatus(Object[] updateStatusObjArry) {
		try {
			postgresTemplate.update(utility.getUpdateBatchDetailStatus(), updateStatusObjArry);
		} catch (Exception e) {
			log.error(AuditMessage.EXECPTION_MSG + "updateBatchDetailStatus", e);
		}
	}

	@Override
	public int getFailedBatchAttempt(BatchDetail batch) {
		try {
			return postgresTemplate.queryForObject(utility.getFailedBatchAttemptQry(batch), Integer.class);
		} catch (Exception e) {
			log.error(AuditMessage.EXECPTION_MSG + "getFailedBatchAttempt", e);
			throw e;
		}
	}

	@Override
	public List<AllTableRows> fetchDataFromDes(String selectDesQry) {
		List<AllTableRows> desTablesRowCuntList = null;
		try {
			desTablesRowCuntList = postgresTemplate.query(selectDesQry, 
					new BeanPropertyRowMapper<AllTableRows>(AllTableRows.class));
		} catch (Exception e) {
			log.error("Method fetchDataFromDes Exception !!!",e);
			throw e;
		}
		return desTablesRowCuntList;
	}

}
