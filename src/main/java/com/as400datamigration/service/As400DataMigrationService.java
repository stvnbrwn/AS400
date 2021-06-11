package com.as400datamigration.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.as400datamigration.common.BatchDetailStatus;
import com.as400datamigration.common.Utility;
import com.as400datamigration.model.BatchDetail;
import com.as400datamigration.model.PostgresQueries;
import com.as400datamigration.model.SQLColumn;
import com.as400datamigration.model.TableMetaData;
import com.as400datamigration.reposistory.As400Dao;
import com.as400datamigration.reposistory.PostgresDao;

@Service
public class As400DataMigrationService {

	@Value("${batch.size}")
	private int batchSize;

	@Autowired
	Utility utility;

	@Autowired
	As400Dao as400Dao;

	@Autowired
	PostgresDao postgresDao;

	public void process(String filePath) {
		Path path;
		List<String> tableList = new ArrayList<>();
		try {
			if (!Objects.isNull(filePath)) {
				path = Paths.get(filePath);
				tableList = Files.readAllLines(path);
			}

			tableList.forEach(tableName -> {
				if (!tableName.isEmpty()) {

					TableMetaData tableMetaData = as400Dao.getTableMetaData(tableName);
					tableMetaData.setTableName(tableName);
					List<SQLColumn> columns = as400Dao.getTableDesc(tableName);
					PostgresQueries postgresQueries = utility.getPostgresQueries(tableName, columns);
					postgresDao.createTable(postgresQueries.getCreateTable(),tableMetaData);
					
					// improve
					List<Object[]> tableData = null;
					if (tableMetaData.getTotalRows() < batchSize) {
						
						BatchDetail batchDetails=new BatchDetail(tableMetaData,BatchDetailStatus.RUNNING);
						// starting time -?? before read from as400 
						postgresDao.saveAllBatchDetail(batchDetails,BatchDetailStatus.RUNNING);
						
						tableData = as400Dao.performOprationOnTable(tableName, tableMetaData.getMinRrn(),
								tableMetaData.getMaxRrn(), columns);
						
						postgresDao.saveBatchInTable(postgresQueries.getInsertTable(), tableData,batchDetails);
						
						
					} else {
						long maxBatchLimit = tableMetaData.getMaxRrn() - batchSize - 1;
						long totalrows = 0;
						while (maxBatchLimit > tableMetaData.getMinRrn()) {
							tableData = as400Dao.performOprationOnTable(tableName, tableMetaData.getMinRrn(),
									tableMetaData.getMinRrn() + batchSize - 1, columns);
							tableMetaData.setMinRrn(tableMetaData.getMinRrn() + batchSize);
							totalrows += tableData.size(); /* maxRrn-=batchSize; */
							postgresDao.saveBatchInTable(postgresQueries.getInsertTable(), tableData);
						}
						tableData=as400Dao.performOprationOnTable(tableName, tableMetaData.getMinRrn(), tableMetaData.getMaxRrn(),
								columns);
						postgresDao.saveBatchInTable(postgresQueries.getInsertTable(), tableData);
					}
					System.out.println("stop !!!");
					// as400Dao.performOprationOnTable(tableName, totalRecords);
					// as400Dao.performOprationOnTable(tableName, columns);

				}
			});

		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
