package com.as400datamigration.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.as400datamigration.audit.BatchDetailStatus;
import com.as400datamigration.audit.TableStatus;
import com.as400datamigration.common.Utility;
import com.as400datamigration.model.AllTableRows;
import com.as400datamigration.model.BatchDetail;
import com.as400datamigration.model.SQLColumn;
import com.as400datamigration.model.SelectQryDesAndSrc;
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

	// testing purpose -- was called from testRunner
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

		TableProcess tableProcessdata;
		try {
			tableProcessdata= postgresDao.getTableMetaData(tableName);
			if (Objects.nonNull(tableProcessdata)) {
				TableSummaryJson tableSummaryJson = tableSummaryMap.get(tableProcessdata.getStatus().toString());
				tableSummary.setResult(tableSummaryJson.getResult());
				tableSummary.setSummary(tableSummaryJson.getSummary());
				tableSummary.setModifiedAt(tableProcessdata.getModifiedAt());
				tableSummary.setTableStatus(tableProcessdata.getStatus());
			}
		} catch (Exception e) {
			tableSummary.setResult("NOT PERFORMED");
			tableSummary.setTableStatus(TableStatus.TABLE_NOT_CREATED);
			tableSummary.setSummary("Table has not performed yet, or may be connection issue.");
			log.error("Exception at getTableSummary !!!", e);
		}
		return tableSummary;
	}

	public void createfailedBatch(int i) {
		try {

			List<BatchDetail> allBatch = new ArrayList<>();
			if (i == 1)
				allBatch = postgresDao.getTenBatch(new ArrayList<Integer>());
			else {
				System.out.println();
				List<Integer> list = new ArrayList<>();
				Collections.addAll(list, 4, 5, 6);
				allBatch = postgresDao.getTenBatch(list);
				System.out.println();
			}

			allBatch.forEach(batch -> {
				if (batch.getBno() % 2 > 0)
					batch.setStatus(BatchDetailStatus.FAILED_AT_DESTINATION);
				else
					batch.setStatus(BatchDetailStatus.FAILED_AT_SOURCE);
				batch.setColumnJson(postgresDao.getTableMetaData(batch.getTableName()).getColumnJson());

				postgresDao.updateBatchDetail(batch.getUpdateObjArray());
				postgresDao.updateTableProcessStatus(
						new TableProcess(batch.getTableName(), TableStatus.MIGRATION_FAILED).getUpdateObjArray());
			});
		} catch (Exception e) {
			log.error("Exception in createfailedBatch ", e);
		}
	}

	/**
	 * @param selectQryDesAndSrc
	 * @param tableList
	 */
	public boolean runSelectDesAndSource(SelectQryDesAndSrc selectQryDesAndSrc, List<String> tableList) {
		List<AllTableRows> tableRowDest;
		List<AllTableRows> tableRowSrc;
		boolean hasMissMatchedRows = false;
		try {
			tableRowDest = postgresDao.fetchDataFromDes(selectQryDesAndSrc.getSelectDenQry());
		} catch (Exception e) {
			throw e;
		}
		tableRowDest = tableRowDest.stream()
		        .sorted((o1, o2) -> {return o1.getTableName().compareTo(o2.getTableName());})
		        .collect(Collectors.toList());
		try {
			tableRowSrc = as400Dao.fetchDataFromSource(selectQryDesAndSrc.getSelectSrcQry());
		} catch (Exception e) {
			throw e;
		}
		tableRowSrc = tableRowSrc.stream()
		        .sorted((o1, o2) -> {return o1.getTableName().compareTo(o2.getTableName());})
		        .collect(Collectors.toList());

		Map<String, String> LibraryAndTableNameMap = new HashMap<>();
		tableList.forEach(table->{
			LibraryAndTableNameMap.put(table.substring(table.lastIndexOf(".")+1),table);
			// error if two library have same table name
		});

		int count = 1;
		for (int i = 0; i < tableRowSrc.size(); i++) {
			if (tableRowSrc.get(i).getTotalRows() != tableRowDest.get(i).getTotalRows()) {
				log.info("Rows are not matched : " + (count++) + " : " + LibraryAndTableNameMap.get(tableRowSrc.get(i).getTableName())   + " Source : "
						+ tableRowSrc.get(i).getTotalRows() + " Destination : " + tableRowDest.get(i).getTotalRows());
				hasMissMatchedRows = true;
			}
		}
		return hasMissMatchedRows;

	}

}
