package com.as400datamigration.model;

import java.time.LocalDateTime;

import com.as400datamigration.audit.TestOutPutStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TableSummary {
	
	
	String tableName;
	TestOutPutStatus  status;
	LocalDateTime modifiedAt;
	String summary;
	
	public TableSummary(String tableName) {
		this.tableName=tableName;
	}

}
