package com.as400datamigration.model;

import java.util.List;
import java.util.Objects;

import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@ToString
public class TableMetaData {

	Long totalRows;
	Long minRrn;
	Long maxRrn;
	String tableName = "";
	List<SQLColumn> columns;
	PostgresQueries postgresQueries;

	TableProcess tableProcess;
	BatchDetail batchDetail;
	FailedBatchDetails failedBatchDetails;

	public TableMetaData(String tableName) {
		this.tableName = tableName;
	}
	
	public TableMetaData(Long totalRows, String tableName) {
		super();
		this.totalRows = totalRows;
		this.tableName = tableName;
	}

	public TableMetaData(Long totalRows, Long minRrn, Long maxRrn) {
		super();
		this.totalRows = totalRows;
		this.minRrn = minRrn;
		this.maxRrn = maxRrn;

	}
	
	public TableMetaData(String tableName,Long totalRows, Long minRrn, Long maxRrn,List<SQLColumn> columns) {
		super();
		this.tableName = tableName;
		this.totalRows = totalRows;
		this.minRrn = minRrn;
		this.maxRrn = maxRrn;
		this.columns=columns;

	}

	public TableMetaData(String tableName, long startingRrn, long endingRrn, List<SQLColumn> columns,
			FailedBatchDetails failedBatchDetails) {
		super();
		this.tableName = tableName;
		this.minRrn = startingRrn;
		this.maxRrn = endingRrn;
		this.columns = columns;
		this.failedBatchDetails = failedBatchDetails;
	}

	public Long getTotalRows() {
		return totalRows;
	}

	public void setTotalRows(Long totalRows) {
		if (Objects.isNull(totalRows))
			this.totalRows = 0l;
		else
			this.totalRows = totalRows;
	}

	public Long getMinRrn() {
		return minRrn;
	}

	public void setMinRrn(Long minRrn) {
		if (Objects.isNull(minRrn))
			this.minRrn = 0l;
		else
			this.minRrn = minRrn;
	}

	public Long getMaxRrn() {
		return maxRrn;
	}

	public void setMaxRrn(Long maxRrn) {
		if (Objects.isNull(maxRrn))
			this.maxRrn = 0l;
		else
			this.maxRrn = maxRrn;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public List<SQLColumn> getColumns() {
		return columns;
	}

	public void setColumns(List<SQLColumn> columns) {
		this.columns = columns;
	}

	public PostgresQueries getPostgresQueries() {
		return postgresQueries;
	}

	public void setPostgresQueries(PostgresQueries postgresQueries) {
		this.postgresQueries = postgresQueries;
	}

	public BatchDetail getBatchDetail() {
		return batchDetail;
	}

	public void setBatchDetail(BatchDetail batchDetail) {
		this.batchDetail = batchDetail;
	}

	public FailedBatchDetails getFailedBatchDetails() {
		return failedBatchDetails;
	}

	public void setFailedBatchDetails(FailedBatchDetails failedBatchDetails) {
		this.failedBatchDetails = failedBatchDetails;
	}

	public TableProcess getTableProcess() {
		return tableProcess;
	}

	public void setTableProcess(TableProcess tableProcess) {
		this.tableProcess = tableProcess;
	}



	

}
