package com.as400datamigration.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/** 
 * This class is entity class for all_table_process_detail table.
 * 
 * @author Programmers.io - Mohit Kachhwaha - created at 01-Jul-2021
 * 	
 * 			Modification - MohitKachhwaha - 01-Jul-2021
 *          
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TableProcessDetail {
	
	String tableName;
	String reason;
	/**
	 * @return  object array which we can use to save data in all_table_process_details
	 */
	public Object[] getSaveObjArray() {
		return new Object[] {
				this.tableName,
				this.reason
		};
	}

}
