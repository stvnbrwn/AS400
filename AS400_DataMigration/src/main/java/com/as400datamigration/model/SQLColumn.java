package com.as400datamigration.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SQLColumn {

	String name;
	String columnType;
	int columnSize;

	public String getCreateString() {
		return name + " " + columnType + " ( " + columnSize + " ) ,";
	}

	public String getInsertString() {
		return name + ", ";
	}

}
