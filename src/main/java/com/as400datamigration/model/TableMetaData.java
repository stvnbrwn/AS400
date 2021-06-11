package com.as400datamigration.model;

import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@ToString
public class TableMetaData {
	
	
	Long totalRows;
	Long minRrn;
	Long maxRrn;
	String tableName="";
	
	public TableMetaData(Long totalRows, Long minRrn, Long maxRrn) {
		super();
		this.totalRows = totalRows;
		
		if(Objects.isNull(minRrn))
			this.minRrn =0l;
		else
			this.minRrn = minRrn;
		
		if(Objects.isNull(maxRrn))
			this.maxRrn =0l;
		else
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
	
	
	
	
}
