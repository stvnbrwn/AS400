package com.as400datamigration.reposistory;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PostgresDao {

	@Autowired
	@Qualifier("PostgresJdbcTemplate")
	private JdbcTemplate postgresTemplate;

	public void createTable(String crtQuery) {
		try {
			log.info("Table creation start :" + LocalDateTime.now());
			postgresTemplate.execute(crtQuery);
			log.info("Table creation end   :" + LocalDateTime.now());
		} catch (Exception e) {
			log.error("Table creation fail !!!");
			e.printStackTrace();
		}

	}

	public void insertBatchInTable(String insertQuery, List<Object[]> tableDataList) {

		try {
			log.info("batch insert start :-" + "batch size : " + tableDataList.size() + LocalDateTime.now());
			postgresTemplate.batchUpdate(insertQuery, tableDataList);
			log.info("batch insert end   :-" + "batch size : " + tableDataList.size() + LocalDateTime.now());
		} catch (Exception e) {
			log.error("Batch insert fail !!!");
			e.printStackTrace();
		}
	}

}
