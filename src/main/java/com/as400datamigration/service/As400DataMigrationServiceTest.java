package com.as400datamigration.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.as400datamigration.audit.BatchDetailStatus;
import com.as400datamigration.audit.TableStatus;
import com.as400datamigration.common.Utility;
import com.as400datamigration.model.BatchDetail;
import com.as400datamigration.model.SQLColumn;
import com.as400datamigration.model.TableProcess;
import com.as400datamigration.model.TableSummary;
import com.as400datamigration.model.TableSummaryJson;
import com.as400datamigration.reposistory.As400Dao;
import com.as400datamigration.reposistory.PostgresDao;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class As400DataMigrationServiceTest {
	@Autowired
	Utility utility;

	@Autowired
	As400Dao as400Dao;

	@Autowired
	PostgresDao postgresDao;

	static int i = 1;

	public void process(String filePath) {
		Path path;
		List<String> tableList = new ArrayList<>();
		try {
			if (!Objects.isNull(filePath)) {
				path = Paths.get(filePath);
				tableList = Files.readAllLines(path);
			}

			tableList.forEach(tableName -> {
				if (!tableName.trim().isEmpty()) {
					log.info("table no : " + i + " table Name " + tableName + "Start Time : " + LocalDateTime.now());
					long totalRecords = as400Dao.gettotalRecords(tableName);
					log.info("Total records in the table  : " + totalRecords + " time : " + LocalDateTime.now());
					boolean atCreation = true;
					List<SQLColumn> columns = as400Dao.getTableDesc(tableName, atCreation);
					log.info("col count : " + columns.size());
					columns.forEach(column -> {
						log.info(column.toString());
					});

					List<Object[]> tableDataList = as400Dao.fetchFirst5RecordsFromTable(tableName, columns);
					log.info("**Table Data**");
					tableDataList.forEach(row -> {
						log.info(Arrays.toString(row));
					});

					log.info("table no : " + i++ + " table Name " + tableName + "Total records : " + totalRecords
							+ "Total Columns : " + columns.size() + "Total fetched data : " + tableDataList.size()
							+ "End Time : " + LocalDateTime.now());
				}
			});

		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public TableSummary getTableSummary(String tableName, Map<String, TableSummaryJson> tableSummaryMap) {

		TableSummary tableSummary = new TableSummary(tableName);
		//List<String> summeryList = new ArrayList<String>();
		
		TableProcess tableProcessdata = postgresDao.getTableMetaData(tableName);
		if (Objects.nonNull(tableProcessdata)) {
				TableSummaryJson tableSummaryJson = tableSummaryMap.get(tableProcessdata.getStatus().toString());
				tableSummary.setStatus(tableSummaryJson.getResult());
				tableSummary.setSummary(tableSummaryJson.getSummary());
				tableSummary.setModifiedAt(tableProcessdata.getModifiedAt());
		} else {
			tableSummary.setStatus("NOT_PERFORMED");
			tableSummary.setSummary(
					"Table has not performed yet, or may be connection issue.");
		}

		return tableSummary;
	}

	public void createfailedBatch(int i) {
		try {
			
			List<BatchDetail> allBatch=new ArrayList<>();
					if(i==1)
						allBatch=postgresDao.getTenBatch(new ArrayList<Integer>());
					else {
						System.out.println();
						List<Integer> list= new ArrayList<>();
						Collections.addAll(list, 4,5,6);
						allBatch=postgresDao.getTenBatch(list);
						System.out.println();
					}
			
			allBatch.forEach(batch->{
				if(batch.getBno()%2>0) 
					batch.setStatus(BatchDetailStatus.FAILED_AT_DESTINATION);
				else
					batch.setStatus(BatchDetailStatus.FAILED_AT_SOURCE);
				batch.setColumnJson(postgresDao.getTableMetaData(batch.getTableName()).getColumnJson());
				
				postgresDao.updateBatchDetail(batch.getUpdateObjArray());
				postgresDao.updateTableProcessStatus(new TableProcess(batch.getTableName(),
						TableStatus.MIGRATION_FAILED).getUpdateObjArray());
			});
		} catch (Exception e) {
			log.error("Exception in createfailedBatch ",e);
		}
	}

}
