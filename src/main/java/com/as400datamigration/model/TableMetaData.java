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
	String tableName="";
	List<SQLColumn> columns;
	PostgresQueries postgresQueries ;
	BatchDetail batchDetail;
	
	public TableMetaData(Long totalRows, Long minRrn, Long maxRrn) {
		super();
		this.totalRows = totalRows;
		this.minRrn = minRrn;
		this.maxRrn = maxRrn;
		
	}

	public Long getTotalRows() {
		return totalRows;
	}

	public void setTotalRows(Long totalRows) {
		this.totalRows = totalRows;
	}

	public Long getMinRrn() {
		return minRrn;
	}

	public void setMinRrn(Long minRrn) {
		if(Objects.isNull(minRrn))
			this.minRrn =0l;
		else
			this.minRrn = minRrn;
	}

	public Long getMaxRrn() {
		return maxRrn;
	}

	public void setMaxRrn(Long maxRrn) {
		if(Objects.isNull(maxRrn))
			this.maxRrn =0l;
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

	
	
}
