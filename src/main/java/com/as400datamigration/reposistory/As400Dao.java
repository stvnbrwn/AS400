package com.as400datamigration.reposistory;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.as400datamigration.model.SQLColumn;
import com.as400datamigration.model.TableMetaData;

@Repository
public interface As400Dao {

	
	// 1) Full insertion 4)TEST
	public long gettotalRecords(String tableName) ;
	
	// 1) Full insertion 4)TEST
	public List<SQLColumn> getTableDesc(String tableName) ;

	// 4)TEST
	public List<Object[]> fetchFirst5RecordsFromTable(String tableName, List<SQLColumn> columns) ;
	
	// 1) full insertion -> get as400 data from tables --> previously get columns data
	//public List<Object[]> performOprationOnTable(String tableName, List<SQLColumn> columns);

	// get all records in
	//public void performOprationOnTable(String tableName, long totalRecords) ;
	// get records in batch
	public List<Object[]> performOprationOnTable(String tableName, long offset, long batchSize,List<SQLColumn> columns);

	//public List<Object[]> performOprationOnTable(String tableName, long totalRecords);

	public TableMetaData getTableMetaData(String tableName);
	
	//public List<Object[]> performOprationOnTable(TableMetaData tableMetaData, List<SQLColumn> columns);
	
}
