package com.example.batch.config;

import com.example.batch.model.DiffRecord;
import com.example.batch.processor.DeleteProcessor;
import com.example.batch.processor.DiffProcessor;
import com.example.batch.reader.BlackListReader;
import com.example.batch.writer.DiffWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    @Autowired
    private DataSource dataSource;

    @Bean
    public JdbcCursorItemReader<DiffRecord> diffReader() {
        JdbcCursorItemReader<DiffRecord> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataSource);
        reader.setSql("SELECT id, col1, col2 FROM A ORDER BY id");
        reader.setRowMapper(new BeanPropertyRowMapper<>(DiffRecord.class));
        return reader;
    }

    @Bean
    public DiffProcessor processor() {
        return new DiffProcessor(dataSource);
    }

    @Bean
    public DiffWriter writer() {
        return new DiffWriter(dataSource);
    }


    @Bean
    public JdbcCursorItemReader<DiffRecord> deleteReader() {
        JdbcCursorItemReader<DiffRecord> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataSource);
        reader.setSql("SELECT id, col1, col2 FROM B ORDER BY id");
        reader.setRowMapper(new BeanPropertyRowMapper<>(DiffRecord.class));
        return reader;
    }

    @Bean
    public DeleteProcessor deleteProcessor() {
        return new DeleteProcessor(dataSource);
    }

    @Bean
    public Step diffStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("diffStep", jobRepository)
                .<DiffRecord, DiffRecord>chunk(1000, transactionManager)
                .reader(diffReader())
                .processor(processor())
                .writer(writer())
                .build();
    }
    @Bean
    public Step deleteStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("deleteStep", jobRepository)
                .<DiffRecord, DiffRecord>chunk(1000, transactionManager)
                .reader(deleteReader())
                .processor(deleteProcessor())
                .writer(writer())
                .build();
    }

    @Bean
    public Job diffJob(JobRepository jobRepository,
                       @Qualifier("diffStep") Step diffStep,
                       @Qualifier("deleteStep") Step deleteStep) {
        return new JobBuilder("diffJob", jobRepository)
                .start(diffStep)
                .next(deleteStep)
                .build();
    }

}
