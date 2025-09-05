package com.example.batch.processor;

import com.example.batch.model.DiffRecord;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.EmptyResultDataAccessException;

import javax.sql.DataSource;

public class DeleteProcessor implements ItemProcessor<DiffRecord, DiffRecord> {

    private final JdbcTemplate jdbcTemplate;

    public DeleteProcessor(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public DiffRecord process(DiffRecord item) throws Exception {
        try {
            // 检查 A 表中是否存在
            jdbcTemplate.queryForObject(
                    "SELECT 1 FROM A WHERE id = ?",
                    Integer.class,
                    item.getId()
            );
            // A 中存在 → 不删除
            return null;
        } catch (EmptyResultDataAccessException e) {
            // A 中不存在 → DELETE
            item.setDiffType("DELETE");
            return item;
        }
    }
}
