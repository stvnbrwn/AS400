package com.as400datamigration.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;


@Repository
public interface PostgresDao {

	
	public void createTable(String crtQuery) ;

	public void insertBatchInTable(String insertQuery, List<Object[]> tableDataList) ;

}
