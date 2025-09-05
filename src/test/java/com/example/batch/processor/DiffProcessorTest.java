package com.example.batch.processor;

import com.example.batch.model.DiffRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

class DiffProcessorTest {

    private DiffProcessor processor;

    @BeforeEach
    void setUp() {
        DataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "");

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        // 创建表 A 和 B
        jdbcTemplate.execute("CREATE TABLE A(id VARCHAR(50), col1 VARCHAR(50), col2 VARCHAR(50))");
        jdbcTemplate.execute("CREATE TABLE B(id VARCHAR(50), col1 VARCHAR(50), col2 VARCHAR(50))");

        // 插入测试数据
        jdbcTemplate.update("INSERT INTO B(id,col1,col2) VALUES('1','b1','b2')");
        jdbcTemplate.update("INSERT INTO A(id,col1,col2) VALUES('1','a1','a2')");
//        jdbcTemplate.update("INSERT INTO A(id,col1,col2) VALUES('2','a3','a4')");


        jdbcTemplate.execute("CREATE TABLE DIFF_TABLE(id VARCHAR(50), col1 VARCHAR(50), col2 VARCHAR(50), diff_type VARCHAR(20))");
        processor = new DiffProcessor(dataSource);
    }

    @Test
    void testProcess_InsertOrUpdate() throws Exception {
        // 测试 ID=2 → INSERT
        DiffRecord record2 = new DiffRecord();
        record2.setId("2");
        record2.setCol1("a3");
        record2.setCol2("a4");

        DiffRecord result2 = processor.process(record2);
        assertEquals("INSERT", result2.getDiffType());

        // 测试 ID=1 → UPDATE
        DiffRecord record1 = new DiffRecord();
        record1.setId("1");
        record1.setCol1("a1");
        record1.setCol2("a2");

        DiffRecord result1 = processor.process(record1);
        assertEquals("UPDATE", result1.getDiffType());
    }
}