package com.as400datamigration.model;

import java.time.LocalDateTime;

import com.as400datamigration.audit.BatchDetailStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class BatchDetail {
	
	String tableName  ;
	long  startingRrn;
	long endingRrn  ;
	
	LocalDateTime startedAtSource;
	LocalDateTime startedAtDestination;
	
	BatchDetailStatus status	;  

	LocalDateTime endedAtSource;
	LocalDateTime  endedAtDestination;
	
	LocalDateTime modifiedAt;
	String reason;
	
	public BatchDetail(TableMetaData tableMetaData) {
		
		this.tableName = tableMetaData.getTableName();
		this.startingRrn = tableMetaData.getMinRrn();
		this.endingRrn = tableMetaData.getMaxRrn();
		this.startedAtSource = LocalDateTime.now();
		this.status = BatchDetailStatus.Started_At_Source;
		this.modifiedAt = LocalDateTime.now();
		
	}

	public Object[] getSaveObjArray() {
		
		return new Object[] {
				this.tableName,     				  
				this.startingRrn,         
				this.endingRrn,           
				this.startedAtSource     ,
				this.startedAtDestination,
				this.status              ,
				this.endedAtSource       ,
				this.endedAtDestination ,	
				this.modifiedAt 		,
				this.reason 		};
	}
	
	public Object[] getUpdateObjArray() {
		
		return new Object[] {
			//update	
				this.startedAtDestination,
				this.status              ,
				this.endedAtSource       ,
				this.endedAtDestination ,	
				this.modifiedAt 		,
				this.reason 		,
			//where
				this.tableName,     				  
				this.startingRrn,         
				this.endingRrn
			};
	}

	

		
}
