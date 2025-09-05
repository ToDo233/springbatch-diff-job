package com.example.batch.processor;

import com.example.batch.model.DiffRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.EmptyResultDataAccessException;

import javax.sql.DataSource;
import java.util.Map;

public class DiffProcessor implements ItemProcessor<DiffRecord, DiffRecord> {

    private final JdbcTemplate jdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(DiffProcessor.class);


    public DiffProcessor(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public DiffRecord process(DiffRecord item) throws Exception {
        try {
            // 从 B 表查找相同主键的记录
            Map<String, Object> bRecord = jdbcTemplate.queryForMap(
                    "SELECT col1, col2 FROM B WHERE id = ?",
                    item.getId()
            );

            // 对比字段是否相同
            String bCol1 = (String) bRecord.get("col1");
            String bCol2 = (String) bRecord.get("col2");

            if (!item.getCol1().equals(bCol1) || !item.getCol2().equals(bCol2)) {
                item.setDiffType("UPDATE");
                logger.info("Processed DiffRecord: id={}, diffType={}", item.getId(), item.getDiffType());

            } else {
                item.setDiffType("NONE"); // 没变化
            }
        } catch (EmptyResultDataAccessException e) {
            // B 表没有 → 新增
            item.setDiffType("INSERT");
        }
        return item;
    }
}
