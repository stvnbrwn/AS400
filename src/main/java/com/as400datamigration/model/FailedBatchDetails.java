package com.as400datamigration.model;

import java.time.LocalDateTime;

import com.as400datamigration.audit.FailBatchStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class FailedBatchDetails {

	  long fbno;
	  long bno ;
	  LocalDateTime startedAt;
	  FailBatchStatus status ;
	  LocalDateTime endedAt ;
	  String reason;
	  
	  public FailedBatchDetails(long bno) {
		  	this.bno = bno;
			this.startedAt = LocalDateTime.now();
			this.status = FailBatchStatus.ATTEMPT;
		}

	public Object[] getSaveObjArray() {
			return new Object[] {
					this.bno ,
					this.startedAt ,
					this.status.toString() ,
					this.endedAt ,
					this.reason 
			};
	}

	public Object[] getUpdateObjArray() {
		return new Object[] {
				//update
				this.status.toString() ,
				this.endedAt ,
				this.reason ,
				//where
				this.fbno 
		};
	}
	  
}
