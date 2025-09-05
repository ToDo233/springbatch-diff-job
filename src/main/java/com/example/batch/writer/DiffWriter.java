package com.example.batch.writer;

import com.example.batch.model.DiffRecord;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

public class DiffWriter implements ItemWriter<DiffRecord> {

    private final JdbcTemplate jdbcTemplate;

    public DiffWriter(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void write(Chunk<? extends DiffRecord> items) throws Exception {
        for (DiffRecord record : items) {
            switch (record.getDiffType()) {
                case "INSERT":
                    jdbcTemplate.update(
                            "INSERT INTO B (id, col1, col2) VALUES (?, ?, ?)",
                            record.getId(), record.getCol1(), record.getCol2()
                    );
                    jdbcTemplate.update(
                            "INSERT INTO DIFF_TABLE (id, col1, col2, diff_type) VALUES (?, ?, ?, 'INSERT')",
                            record.getId(), record.getCol1(), record.getCol2()
                    );
                    break;

                case "UPDATE":
                    jdbcTemplate.update(
                            "UPDATE B SET col1 = ?, col2 = ? WHERE id = ?",
                            record.getCol1(), record.getCol2(), record.getId()
                    );
                    jdbcTemplate.update(
                            "INSERT INTO DIFF_TABLE (id, col1, col2, diff_type) VALUES (?, ?, ?, 'UPDATE')",
                            record.getId(), record.getCol1(), record.getCol2()
                    );
                    break;

                case "DELETE":
                    jdbcTemplate.update(
                            "DELETE FROM B WHERE id = ?",
                            record.getId()
                    );
                    jdbcTemplate.update(
                            "INSERT INTO DIFF_TABLE (id, col1, col2, diff_type) VALUES (?, ?, ?, 'DELETE')",
                            record.getId(), record.getCol1(), record.getCol2()
                    );
                    break;

                case "NONE":
                    // 无变化，不做处理
                    break;

                default:
                    throw new IllegalStateException("未知的 diffType: " + record.getDiffType());
            }
        }
    }
}
