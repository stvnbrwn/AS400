package com.as400datamigration.model;

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
public class SQLColumn {
	
	
	String name;
	String columnType;
	int columnSize;
	int scale;
	String columnHeading;
	

}
