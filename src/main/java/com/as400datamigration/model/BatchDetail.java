package com.as400datamigration.model;

import java.time.LocalDateTime;

import com.as400datamigration.common.BatchDetailStatus;

import lombok.AllArgsConstructor;
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
	LocalDateTime startedAt;  
	BatchDetailStatus status	;    
	LocalDateTime endedAt ;
	LocalDateTime modifiedAt;
	
	

	public BatchDetail(String tableName, Long minRrn, Long maxRrn, LocalDateTime startedAt, BatchDetailStatus status,
			LocalDateTime endedAt, LocalDateTime modifiedAt) {
	}



	public BatchDetail(TableMetaData tableMetaData, BatchDetailStatus status) {
		this.tableName=tableMetaData.getTableName();
		this.startingRrn=tableMetaData.getMinRrn();
		this.endingRrn=tableMetaData.getMaxRrn();
		this.startedAt=LocalDateTime.now();
		this.status=BatchDetailStatus.RUNNING;
		this.endedAt=null;
		this.modifiedAt = LocalDateTime.now();

	}



	public Object[] getObjArray() {
		
		return new Object[] {this.getTableName(),this.getStartingRrn(),
							this.getEndingRrn(),this.getStartedAt(),
							this.getStatus(),this.getEndedAt(),
							this.getModifiedAt()};
	}



	/*
	 * public BatchDetail getObjArrayOnstatus(AllBatchDetailStatus status) {
	 * 
	 * if(status.equals(AllBatchDetailStatus.RUNNING)) return this; else if
	 * (status.equals(AllBatchDetailStatus.COMPLETED)) return new
	 * BatchDetail(this,AllBatchDetailStatus.COMPLETED);
	 * 
	 * 
	 * }
	 * 
	 * 
	 * 
	 * public BatchDetail(BatchDetail allBatchDetails, AllBatchDetailStatus
	 * completed) { // TODO Auto-generated constructor stub }
	 */
}
