package com.as400datamigration.reposistory;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.as400datamigration.model.SQLColumn;
import com.as400datamigration.model.TableMetaData;

@Repository
public interface As400Dao {

	// 1) Full insertion 4)TEST
	public long gettotalRecords(String tableName);

	// 1) Full insertion 4)TEST
	public List<SQLColumn> getTableDesc(String tableName);

	// 4)TEST
	public List<Object[]> fetchFirst5RecordsFromTable(String tableName, List<SQLColumn> columns);

	public List<Object[]> performReadOprationOnTable(String tableName, long MinRrn, long Offset,
			List<SQLColumn> columns);

	public TableMetaData getTableMetaData(String tableName);

	public List<Object[]> readOprationOnTable(TableMetaData tableMetaData);

}
