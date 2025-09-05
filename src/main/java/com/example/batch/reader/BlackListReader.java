package com.example.batch.reader;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class BlackListReader {

    @Bean
    @StepScope
    public JdbcPagingItemReader<Map<String, Object>> reader(
            DataSource dataSource,
            @Value("#{stepExecutionContext['partitionIndex']}") int partitionIndex,
            @Value("#{stepExecutionContext['partitionCount']}") int partitionCount) {

        JdbcPagingItemReader<Map<String, Object>> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(dataSource);
        reader.setFetchSize(1000);

        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(dataSource);
        queryProvider.setSelectClause("id, col1, col2, col3");
        queryProvider.setFromClause("from A");
        // MySQL: CRC32，Oracle: ORA_HASH
        queryProvider.setWhereClause("MOD(CRC32(id), " + partitionCount + ") = " + partitionIndex);
        queryProvider.setSortKey("id");

        try {
            reader.setQueryProvider(queryProvider.getObject());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        reader.setRowMapper((rs, rowNum) -> {
            Map<String, Object> row = new HashMap<>();
            row.put("id", rs.getString("id"));
            row.put("col1", rs.getString("col1"));
            row.put("col2", rs.getString("col2"));
            row.put("col3", rs.getString("col3"));
            return row;
        });

        return reader;
    }
}
