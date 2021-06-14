package com.as400datamigration.reposistory;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.as400datamigration.model.TableMetaData;

@Repository
public interface PostgresDao {

	public void saveIntoTableProcess(Object[] tableProcess);

	public void updateTableProcessStatus(Object[] allTableProcess);

	public void createTable(TableMetaData tableMetaData);

	public void saveBatchDetail(Object[] allBatchDetails);

	public void updateBatchDetail(Object[] allBatchDetails);

	public boolean writeOpraionOnTable(TableMetaData tableMetaData, List<Object[]> tableData);

}
