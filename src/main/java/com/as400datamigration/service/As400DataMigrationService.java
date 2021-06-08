package com.as400datamigration.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.as400datamigration.reposistory.As400Dao;
import com.as400datamigration.reposistory.PostgresDao;


@Service
public class As400DataMigrationService {
	@Autowired
	As400Dao as400Dao;

	@Autowired
	PostgresDao postgresDao;

	public void process(String filePath) {

		try {
			Path path = Paths.get(filePath);
			List<String> tableList = Files.readAllLines(path);

			tableList.forEach(tableName -> {
				
				
				if (!tableName.isEmpty()) {
					
					long totalRecords = as400Dao.gettotalRecords(tableName);
					
					as400Dao.getTableDesc(tableName);
					/*
					 * (if totalRecords is greater than batch size then --> noraml select query with
					 * fetch clause else use fetch and create new method for thread )
					 */
					
					as400Dao.performOprationOnTable(tableName,totalRecords);
					
				}
				/*
				 * tableData.forEach(dataarray->{ Arrays.asList(dataarray).forEach(data->{
				 * System.out.print(" | " +data + " | "); }); System.out.println(); });
				 */

				/*
				 * if(!tableData.isEmpty()) postgresDao.setTableData(tableData);
				 */
			});

		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
