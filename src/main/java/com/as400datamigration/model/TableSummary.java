package com.as400datamigration.model;

import java.time.LocalDateTime;

import com.as400datamigration.audit.TableStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TableSummary {
	
	
	String tableName;
	String  result;
	LocalDateTime modifiedAt;
	String summary;
	
	TableStatus tableStatus;
	
	public TableSummary(String tableName) {
		this.tableName=tableName;
	}

	@Override
	public String toString() {
		return "TableSummary [Table name=" + tableName + ", Result=" + result + ", Modified at=" + modifiedAt
				+ ", Summary=" + summary + "]";
	}
	
	

}
