package com.as400datamigration.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.as400datamigration.audit.AuditMessage;
import com.as400datamigration.audit.TableStatus;
import com.as400datamigration.common.Utility;
import com.as400datamigration.model.BatchDetail;
import com.as400datamigration.model.SQLColumn;
import com.as400datamigration.model.TableMetaData;
import com.as400datamigration.model.TableProcess;
import com.as400datamigration.reposistory.As400Dao;
import com.as400datamigration.reposistory.PostgresDao;

import lombok.extern.slf4j.Slf4j;

@Slf4j
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
					TableMetaData tableMetaData = createTable(tableName);
					List<Object[]> tableData = null;

					if (Objects.nonNull(tableMetaData)) {
						if (tableMetaData.getTotalRows() > 0) {
							long maxRrn = tableMetaData.getMaxRrn();
							// long totalrows = 0; // extra
							boolean allBatchInsert = true;
							while (tableMetaData.getMinRrn() < maxRrn) {
								tableMetaData.setMaxRrn(tableMetaData.getMinRrn() + batchSize - 1);
								if (tableMetaData.getMaxRrn() > maxRrn) {
									tableMetaData.setMaxRrn(maxRrn);
								}
								tableMetaData.setBatchDetail(new BatchDetail(tableMetaData));
								tableData = as400Dao.readOprationOnTable(tableMetaData);
								if (Objects.nonNull(tableData)) {
									boolean batchInsert = postgresDao.writeOpraionOnTable(tableMetaData, tableData);
									allBatchInsert = allBatchInsert && batchInsert;

								} else {
									allBatchInsert = false;
								}

								tableMetaData.setMinRrn(tableMetaData.getMinRrn() + batchSize);
								// totalrows += tableData.size(); // extra
							}
							if (allBatchInsert) {
								postgresDao.updateTableProcessStatus(new TableProcess(tableMetaData.getTableName(),
										TableStatus.Table_Created_And_AllBatchCompleted).getUpdateObjArray());
							}

						}
						System.out.println("stop !!!");
					}
				}
			});

		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Transactional
	private TableMetaData createTable(String tableName) {
		TableMetaData tableMetaData = null;
		TableProcess tableProcess = new TableProcess(tableName);
		try {
			tableMetaData = as400Dao.getTableMetaData(tableName);
			if (Objects.nonNull(tableMetaData)) {
				tableMetaData.setTableName(tableName);
				List<SQLColumn> columns = as400Dao.getTableDesc(tableName);
				if (Objects.nonNull(columns)) {
					tableMetaData.setColumns(columns);

					tableMetaData.setPostgresQueries(utility.getPostgresQueries(tableMetaData));

					postgresDao.createTable(tableMetaData);

					if (tableMetaData.getTotalRows() > 0) {
						tableProcess.setTotalRows(tableMetaData.getTotalRows());
						tableProcess.setStatus(TableStatus.Table_Created_And_InRunning);
					} else {
						tableProcess.setTotalRows(0l);
						tableProcess.setStatus(TableStatus.Table_Created_With_NO_Data);
					}

					// tableMetaData.setTableProcess(tableProcess);
					postgresDao.saveIntoTableProcess(tableProcess.getSaveObjArray());
				}
			}

		} catch (Exception e) {
			log.info("Exception At create Table !!! ", e);
			tableProcess.setTotalRows(0l);
			tableProcess.setStatus(TableStatus.Table_Creation_Failed);
			tableProcess.setReason(AuditMessage.Table_Creation_Failed_Msg + AuditMessage.Execption_Msg + e);
			postgresDao.saveIntoTableProcess(tableProcess.getSaveObjArray());
		}
		return tableMetaData;
	}

}
