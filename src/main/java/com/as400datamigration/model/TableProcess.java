package com.as400datamigration.model;

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
	TableStatus status;
	String reason;

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

		return new Object[] { this.getTableName(), this.getTotalRows(), this.getStatus(), this.getReason() };

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

	public Object[] getUpdateObjArray() {

		return new Object[] { this.status, this.reason, this.tableName };

	}

}
