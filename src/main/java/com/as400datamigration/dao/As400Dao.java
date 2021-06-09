package com.as400datamigration.dao;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.as400datamigration.common.Utility;
import com.as400datamigration.model.SQLColumn;

import lombok.extern.slf4j.Slf4j;

@Repository
public interface As400Dao {

	
	// 1) Full insertion 4)TEST
	public long gettotalRecords(String tableName) ;
	
	// 1) Full insertion 4)TEST
	public List<SQLColumn> getTableDesc(String tableName) ;

	// 4)TEST
	public List<Object[]> fetchFirst5RecordsFromTable(String tableName, List<SQLColumn> columns) ;
	
	// 1) full insertion -> get as400 data from tables
	public List<Object[]> performOprationOnTable(String tableName, List<SQLColumn> columns);

	public void performOprationOnTable(String tableName, long totalRecords) ;


}
