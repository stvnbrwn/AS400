package com.as400datamigration.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/** 
 * This class for
 * 
 * @author Programmers.io - Mohit Kachhwaha - 15-Jul-2021
 * 	
 * 			Modification - MohitKachhwaha - 15-Jul-2021
 *          
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SelectQryDesAndSrc {
	
	String selectDenQry;
	String selectSrcQry;
	
	
}
