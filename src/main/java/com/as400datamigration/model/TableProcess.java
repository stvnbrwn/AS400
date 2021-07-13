package com.as400datamigration.model;

import java.time.LocalDateTime;

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
	LocalDateTime modifiedAt;
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
	
	public TableProcess(String tableName, TableStatus status , String reason) {
		this.tableName = tableName;
		this.status = status;
		this.createdAt=LocalDateTime.now();
	}

	public Object[] getSaveObjArray() {

		return new Object[] { 
				this.tableName,
				this.totalRows,
				this.minRrn,
				this.maxRrn,
				this.status.toString(),
				this.columnJson,
				this.createdAt=LocalDateTime.now()
		};

	}
	
	/**
	 * @return object array which we can use for update all details in all_table_process
	 */
	public Object[] getTableDetailsWithColumnsObjArray() {
		return new Object[] { 
				//update
				this.totalRows,
				this.minRrn,
				this.maxRrn,
				this.status.toString(),
				this.columnJson,
				//where
				this.tableName
		};
	}
	
	/**
	 * @return object array which we can use for update all details without columns in all_table_process
	 */
	public Object[] getTableDetailsWithoutColumnsObjArray() {
		return new Object[] { 
				//update
				this.totalRows,
				this.minRrn,
				this.maxRrn,
				this.status.toString(),
				//where
				this.tableName
		};
	}
	
	
	
	/**
	 * @return object array which we can use for update status in all_table_process
	 */
	public Object[] getUpdateObjArray() {

		return new Object[] { 
				//update
				this.status.toString(),
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


	public Long getMaxRrn() {
		return this.maxRrn;
	}

	public void setMaxRrn(Long maxRrn) {
		this.maxRrn = maxRrn;
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

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public String getColumnJson() {
		return columnJson;
	}

	public void setColumnJson(String columnJson) {
		this.columnJson = columnJson;
	}

	public LocalDateTime getModifiedAt() {
		return modifiedAt;
	}

	public void setModifiedAt(LocalDateTime modifiedAt) {
		this.modifiedAt = modifiedAt;
	}

	

}
