package com.as400datamigration.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.as400datamigration.model.SQLColumn;
import com.as400datamigration.reposistory.As400Dao;
import com.as400datamigration.reposistory.PostgresDao;

import jdk.internal.org.jline.utils.Log;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class As400DataMigrationServiceTest {
	@Autowired
	As400Dao as400Dao;
	
	static int i=1;

	@Autowired
	PostgresDao postgresDao;

	public void process(String filePath) {

		try {
			Path path = Paths.get(filePath);
			List<String> tableList = Files.readAllLines(path);
			
			tableList.forEach(tableName -> {
				
				
				if (!tableName.trim().isEmpty()) {
					
					log.info("new table start");
					System.out.println("no"+ i++ +"table Name " +tableName);
					
					long totalRecords = as400Dao.gettotalRecords(tableName);
					
					List<SQLColumn> columns=as400Dao.getTableDesc(tableName);
					
					as400Dao.fatchTable5Records(tableName, columns);
					
					
					/*
					 * (if totalRecords is greater than batch size then --> noraml select query with
					 * fetch clause else use fetch and create new method for thread )
					 */
					
					//as400Dao.performOprationOnTable(tableName,totalRecords);
					
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
