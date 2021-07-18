package com.as400datamigration.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/** 
 * This class for
 * 
 * @author Programmers.io - Mohit Kachhwaha - 16-Jul-2021
 * 	
 * 			Modification - MohitKachhwaha - 16-Jul-2021
 *          
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AllTableRows {
	
	int totalRows;
	String tableName;

}
