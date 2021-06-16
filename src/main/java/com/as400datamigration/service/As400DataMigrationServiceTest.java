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

import com.as400datamigration.model.SQLColumn;
import com.as400datamigration.reposistory.As400Dao;
import com.as400datamigration.reposistory.PostgresDao;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class As400DataMigrationServiceTest {
	@Autowired
	As400Dao as400Dao;

	static int i = 1;

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
				if (!tableName.trim().isEmpty()) {
					log.info("table no : " + i + " table Name " + tableName + "Start Time : " + LocalDateTime.now());
					long totalRecords = as400Dao.gettotalRecords(tableName);
					log.info("Total records in the table  : " + totalRecords + " time : " + LocalDateTime.now());
					boolean atCreation=true;
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
}
