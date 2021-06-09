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

import com.as400datamigration.model.SQLColumn;
import com.as400datamigration.model.TableMetaData;
import com.as400datamigration.reposistory.As400Dao;
import com.as400datamigration.reposistory.PostgresDao;

@Service
public class As400DataMigrationService {
	
	@Value("${batch.size}")
	private int batchSize;
	
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
					//long totalRecords = as400Dao.gettotalRecords(tableName);
					TableMetaData tableMetaData=as400Dao.getTableMetaData(tableName);
					tableMetaData.setTableName(tableName);
					/*
					 * (if totalRecords is greater than batch size then --> noraml select query with
					 * fetch clause else use fetch and create new method for thread )
					 */
					
					List<SQLColumn> columns = as400Dao.getTableDesc(tableName);
					//name=RRN, columnType=DECIMAL, columnSize=17
					
					//improve
					if(tableMetaData.getTotalRows()<batchSize)
						//remove min RRn and max RRn
						as400Dao.performOprationOnTable(tableName,tableMetaData.getTotalRows());
					else {
						long maxRrn=tableMetaData.getMaxRrn();
						long minRrn=tableMetaData.getMinRrn();
						long totalrows=0;
						
						while((maxRrn-batchSize-1)>minRrn) {
							List<Object[]> tableData=as400Dao.performOprationOnTable(tableName, minRrn, minRrn+batchSize-1, columns);
							minRrn+=batchSize;
							/* maxRrn-=batchSize; */
							totalrows+=tableData.size();
						}
						if (maxRrn != 0) {
							as400Dao.performOprationOnTable(tableName, minRrn, maxRrn, columns);
						}
					}
						
					//as400Dao.performOprationOnTable(tableName, totalRecords);
					//as400Dao.performOprationOnTable(tableName, columns);
					
				}
			});

		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
