package com.example.batch.job;


import com.example.batch.SpringBatchDiffJobApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@SpringBatchTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.batch.initialize-schema=always",
        "spring.batch.job.enabled=false"
})

class DiffJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void init() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("RUNSCRIPT FROM 'classpath:org/springframework/batch/core/schema-h2.sql'");

        // 创建表 A 和 B
        jdbcTemplate.execute("CREATE TABLE A(id VARCHAR(50), col1 VARCHAR(50), col2 VARCHAR(50))");
        jdbcTemplate.execute("CREATE TABLE B(id VARCHAR(50), col1 VARCHAR(50), col2 VARCHAR(50))");

        // 插入测试数据
        jdbcTemplate.update("INSERT INTO B(id,col1,col2) VALUES('1','b1','b2')");
        jdbcTemplate.update("INSERT INTO A(id,col1,col2) VALUES('1','a1','a2')");
        jdbcTemplate.update("INSERT INTO A(id,col1,col2) VALUES('2','a3','a4')");


        jdbcTemplate.execute("CREATE TABLE DIFF_TABLE(id VARCHAR(50), col1 VARCHAR(50), col2 VARCHAR(50), diff_type VARCHAR(20))");
    }

    @Test
    void testDiffJobExecution() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
    }
}
