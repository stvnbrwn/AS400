package com.as400datamigration.model;

import java.time.LocalDateTime;
import java.util.Objects;

import com.as400datamigration.audit.TableStatus;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TableProcess {

	String tableName;
	Long totalRows;
	Long minRrn;
	Long maxRrn;
	LocalDateTime createdAt;
	TableStatus status;
	String reason="";
	String columnJson;
	

	public TableProcess(String tableName) {
		super();
		this.tableName = tableName;
	}

	public TableProcess(String tableName, TableStatus status) {
		super();
		this.tableName = tableName;
		this.status = status;
	}

	public Object[] getSaveObjArray() {

		return new Object[] { 
				this.tableName,
				this.totalRows,
				this.minRrn,
				this.maxRrn,
				this.status.toString(),
				this.reason,
				this.columnJson,
				this.createdAt
		};

	}
	
	public Object[] getUpdateObjArray() {

		return new Object[] { 
				//update
				this.status.toString(),
				this.reason,
				//where
				this.tableName };

	}
	
	
	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public Long getTotalRows() {
		return totalRows;
	}

	public void setTotalRows(Long totalRows) {
		this.totalRows = totalRows;
	}

	public TableStatus getStatus() {
		return status;
	}

	public void setStatus(TableStatus status) {
		this.status = status;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		if (Objects.isNull(reason)) {
			reason = "";
		}
		this.reason = reason;
	}

	

	public Long getMaxRrn() {
		return this.maxRrn;
	}

	public void setMaxRrn(Long maxRrn) {
		this.maxRrn = maxRrn;
	}

	public TableProcess(String tableName, TableStatus status , String reason) {
		this.tableName = tableName;
		this.status = status;
		this.reason=reason;
		this.createdAt=LocalDateTime.now();
		
	}

	public String getColumnsJson() {
		return columnJson;
	}

	public void setColumnsJson(String columnsJson) {
		this.columnJson = columnsJson;
	}

	public Long getMinRrn() {
		return minRrn;
	}

	public void setMinRrn(Long minRrn) {
		this.minRrn = minRrn;
	}

	public LocalDateTime getCreateAt() {
		return createdAt;
	}

	public void setCreateAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

}
