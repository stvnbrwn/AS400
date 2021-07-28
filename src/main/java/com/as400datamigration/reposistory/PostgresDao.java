package com.as400datamigration.reposistory;

import java.sql.SQLException;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.as400datamigration.model.BatchDetail;
import com.as400datamigration.model.TableMetaData;
import com.as400datamigration.model.TableProcess;
import com.as400datamigration.model.AllTableRows;

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

	public boolean writeOpraionFailedBatch(TableMetaData tableMetaData, List<Object[]> tableData);

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

	public List<BatchDetail> getTenBatch(List<Integer> batchNoList) throws SQLException;

	public void updateBatchDetailStatus(Object[] updateStatusObjArry);

	public int getFailedBatchAttempt(BatchDetail batch);

	/**
	 * @param selectDesQry
	 * @return list of rows for all table
	 */
	public List<AllTableRows> fetchDataFromDes(String selectDesQry);

	
	
}
