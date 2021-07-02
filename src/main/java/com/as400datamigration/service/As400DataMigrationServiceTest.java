package com.as400datamigration.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.as400datamigration.audit.AuditMessage;
import com.as400datamigration.audit.TableStatus;
import com.as400datamigration.audit.TestOutPutStatus;
import com.as400datamigration.common.Utility;
import com.as400datamigration.model.BatchDetail;
import com.as400datamigration.model.SQLColumn;
import com.as400datamigration.model.TableMetaData;
import com.as400datamigration.model.TableProcess;
import com.as400datamigration.model.TableSummary;
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

	public TableSummary getTableSummary(String tableName) {

		TableSummary tableSummary = new TableSummary(tableName);
		//List<String> summeryList = new ArrayList<String>();

		TableProcess tableProcessdata = postgresDao.getTableMetaData(tableName);
		if (Objects.nonNull(tableProcessdata)) {
			if (tableProcessdata.getStatus().equals(TableStatus.TABLE_NOT_FOUND_AT_SOURCE)) {
				tableSummary.setSummary("Table was Not Found At Source.");
				tableSummary.setTestOutPutStatus(TestOutPutStatus.FAIL);
			} else if (tableProcessdata.getStatus().equals(TableStatus.TABLE_DESC_NOT_FOUND_AT_SOURCE)) {
				tableSummary.setSummary("Table's Columns Details Are Not Found At Source.");
				tableSummary.setTestOutPutStatus(TestOutPutStatus.FAIL);
			} else if (tableProcessdata.getStatus().equals(TableStatus.TABLE_CREATION_FAILED)) {
				tableSummary.setSummary("Table Creation Failed,  Table was performed previously "
						+ "but batch processing was not started." + "for more details please check all_table_process_details");
				tableSummary.setTestOutPutStatus(TestOutPutStatus.FAIL);
			}else if (tableProcessdata.getStatus().equals(TableStatus.TABLE_CREATED_AND_IN_RUNNING)) {
				tableSummary.setSummary("Table Created And Start Performming But Data Is Not Completely Migrated. batch fail !!!");
				tableSummary.setTestOutPutStatus(TestOutPutStatus.FAIL);
			}else if (tableProcessdata.getStatus().equals(TableStatus.TABLE_CREATED_WITH_NO_DATA)) {
				tableSummary.setSummary("Table Created With NO Data.");
				tableSummary.setTestOutPutStatus(TestOutPutStatus.PASS);
			} else if (tableProcessdata.getStatus().equals(TableStatus.TABLE_CREATED_AND_ALL_BATCH_COMPLETED)) {
				tableSummary.setSummary("Table Created And All Batch Completed");
				tableSummary.setTestOutPutStatus(TestOutPutStatus.PASS);
			}
			tableSummary.setModifiedAt(tableProcessdata.getCreateAt());
		} else {
			tableSummary.setTestOutPutStatus(TestOutPutStatus.NOT_PERFORMED);
			tableSummary.setSummary(
					"Table has not performed yet, or may be connection issue.");
		}

		return tableSummary;
	}

	/*
	 * private TableMetaData verifyAtSource(String tableName, List<TestOutPut>
	 * testingOutputDestinationList) { TableMetaData tableMetaDataSource = null; try
	 * { tableMetaDataSource = as400Dao.getTableMetaData(tableName, false);
	 * testingOutputDestinationList .add(new TestOutPut(tableName,
	 * TestOutPutStatus.PASS, AuditMessage.Table_Found_At_Source_Msg)); } catch
	 * (Exception e) { log.error(tableName + "Found at Source ...!!");
	 * testingOutputDestinationList .add(new TestOutPut(tableName,
	 * TestOutPutStatus.PASS, AuditMessage.Table_Not_Found_At_Source_Msg)); } return
	 * tableMetaDataSource; }
	 * 
	 * private TableProcess verifyAtDestination(String tableName, List<TestOutPut>
	 * testingOutputSourceList) { TableProcess tableMetaDataDestination = null; try
	 * { tableMetaDataDestination =
	 * postgresDao.getTableMetaDataFromDestination(tableName); } catch (Exception e)
	 * { log.error(tableName + "Found at Destination ...!!");
	 * testingOutputSourceList.add( new TestOutPut(tableName, TestOutPutStatus.PASS,
	 * AuditMessage.Table_Not_Found_At_Destination_Msg)); } return
	 * tableMetaDataDestination; }
	 */

	/*
	 * List<TestOutPut> testingOutputSourceList=new ArrayList<>(); List<TestOutPut>
	 * testingOutputDestinationList=new ArrayList<>();
	 * 
	 * List<TestOutPut> ResultList=new ArrayList<>();
	 * 
	 * TableMetaData tableMetaDataSource = verifyAtSource(tableName,
	 * testingOutputSourceList); if(Objects.nonNull(tableMetaDataSource)) {
	 * TableProcess tableMetaDataDestination
	 * =verifyAtDestination(tableName,testingOutputDestinationList);
	 * if(Objects.nonNull(tableMetaDataDestination)) {
	 * 
	 * 
	 * 
	 * } }
	 */

	/*
	 * if(tableProcessdata.getTotalRows()>0) { BatchDetail lastBatchDetail=
	 * postgresDao.getlastBatchDetails(tableName);
	 * testOutPut.setModifiedAt(lastBatchDetail.getModifiedAt()); } else {
	 * testOutPut.setModifiedAt(tableProcessdata.get);
	 * summeryList.add(tableProcessdata.getStatus().toString()); }
	 */
}
