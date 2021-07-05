package com.as400datamigration.reposistory;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.as400datamigration.model.BatchDetail;
import com.as400datamigration.model.TableMetaData;
import com.as400datamigration.model.TableProcess;

@Repository
public interface PostgresDao {

	public void saveIntoTableProcess(Object[] tableProcess);

	public void updateTableProcessStatus(Object[] allTableProcess);

	public void createTable(TableMetaData tableMetaData);

	public long saveBatchDetail(Object[] allBatchDetails);

	public void updateBatchDetail(Object[] allBatchDetails);

	public boolean writeOpraionOnTable(TableMetaData tableMetaData, List<Object[]> tableData);

	public List<BatchDetail> getfailedbatch();

	public long saveFailedBatchDetail(Object[] saveObjArray);

	public void updateFailedBatchDetail(Object[] updateObjArray);

	public void writeOpraionFailedBatch(TableMetaData tableMetaData, List<Object[]> tableData);

	public TableProcess getTableMetaData(String tableName);

	public BatchDetail getlastBatchDetails(String tableName);

	/**
	 * @param TableProcessDetail's ObjArray
	 */
	public void saveIntoTableProcessDetail(Object[] saveObjArray);

	/**
	 * @param tableDetailsObjArray
	 * @param withCoulmns
	 */
	public void updateTableDeatil(Object[] tableDetailsObjArray,boolean withCoulmns);

	
}
