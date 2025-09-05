package com.example.batch.writer;

import com.example.batch.model.DiffRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.Chunk;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

class DiffWriterTest {

    private DiffWriter writer;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        DataSource ds = new DriverManagerDataSource(
                "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "");
        jdbcTemplate = new JdbcTemplate(ds);


        writer = new DiffWriter(ds);
    }

    @Test
    void testWrite_InsertUpdateDelete() throws Exception {
        DiffRecord insert = new DiffRecord();
        insert.setId("1");
        insert.setCol1("x1");
        insert.setCol2("x2");
        insert.setDiffType("INSERT");

        DiffRecord update = new DiffRecord();
        update.setId("2");
        update.setCol1("y1");
        update.setCol2("y2");
        update.setDiffType("UPDATE");

        DiffRecord delete = new DiffRecord();
        delete.setId("3");
        delete.setCol1("z1");
        delete.setCol2("z2");
        delete.setDiffType("DELETE");

        // 将 List 转为 Chunk
        Chunk<DiffRecord> chunk = new Chunk<>(java.util.List.of(insert, update, delete));

        writer.write(chunk);

        // 检查 DIFF_TABLE
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM DIFF_TABLE", Integer.class);
        assertEquals(3, count);
    }
}
