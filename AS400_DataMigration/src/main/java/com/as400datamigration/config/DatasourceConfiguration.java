package com.as400datamigration.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DatasourceConfiguration {

	@Primary
	@Bean(name = "as400DataSource")
	@ConfigurationProperties(prefix = "as400.datasource")
	public DataSource as400DataSource() {
		return DataSourceBuilder.create().build();
	}

	@Bean(name = "postgresDataSource")
	@ConfigurationProperties(prefix = "postgres.datasource")
	public DataSource postgresDataSource() {
		return DataSourceBuilder.create().build();
	}

	@Bean(name = "As400JdbcTemplate")
	public JdbcTemplate As400JdbcTemplate(@Qualifier("as400DataSource") DataSource ds) {
		return new JdbcTemplate(ds);
	}

	@Bean(name = "PostgresJdbcTemplate")
	public JdbcTemplate PostgresJdbcTemplate(@Qualifier("postgresDataSource") DataSource ds) {
		return new JdbcTemplate(ds);
	}

}
