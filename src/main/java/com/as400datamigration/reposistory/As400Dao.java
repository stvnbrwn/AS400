package com.as400datamigration.reposistory;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.as400datamigration.model.SQLColumn;
import com.as400datamigration.model.TableMetaData;
import com.as400datamigration.model.AllTableRows;

@Repository
public interface As400Dao {

	// 1) Full insertion 4)TEST
	public long gettotalRecords(String tableName);

	// 1) Full insertion 4)TEST
	public List<SQLColumn> getTableDesc(String tableName, boolean atCreation);

	// 4)TEST
	public List<Object[]> fetchFirst5RecordsFromTable(String tableName, List<SQLColumn> columns);

	public TableMetaData getTableMetaData(String tableName,boolean fromTableCreate);

	public List<Object[]> readOprationOnTable(TableMetaData tableMetaData);

	public List<Object[]> readOprationOnFailedBatch(TableMetaData tableMetaData);

	public List<SQLColumn> getTableDesc(TableMetaData tableMetaData,boolean isCreate);

	/**
	 * @param selectSrcQry
	 * @return list of rows for all table
	 */
	public List<AllTableRows> fetchDataFromSource(String selectSrcQry);

	
}
