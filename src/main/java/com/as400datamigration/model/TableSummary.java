package com.as400datamigration.model;

import java.time.LocalDateTime;

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
	String  status;
	LocalDateTime modifiedAt;
	String summary;
	
	public TableSummary(String tableName) {
		this.tableName=tableName;
	}

}
