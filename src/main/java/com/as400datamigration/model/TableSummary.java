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
	String  status;
	LocalDateTime modifiedAt;
	String summary;
	
	TableStatus tableStatus;
	
	public TableSummary(String tableName) {
		this.tableName=tableName;
	}

	@Override
	public String toString() {
		return "TableSummary [tableName=" + tableName + ", status=" + status + ", modifiedAt=" + modifiedAt
				+ ", summary=" + summary + "]";
	}
	
	

}
