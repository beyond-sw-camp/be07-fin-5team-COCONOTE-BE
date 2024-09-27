package com.example.coconote.config.cleanUpConfig;

import com.example.coconote.global.fileUpload.entity.FileEntity;
import com.example.coconote.global.fileUpload.service.S3Service;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.Collections;

@Configuration
@EnableBatchProcessing
public class FileCleanupBatchConfig {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private S3Service s3Service;

    @Bean
    public Job fileCleanupJob(JobRepository jobRepository) {
        return new JobBuilder("fileCleanupJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(findAndDeleteFilesStep())
                .build();
    }

    @Bean
    public Step findAndDeleteFilesStep() {
        return new StepBuilder("findAndDeleteFilesStep", jobRepository)
                .<FileEntity, FileEntity>chunk(10, transactionManager)
                .reader(fileReader())
                .processor(fileProcessor())
                .writer(fileWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<FileEntity> fileReader() {
        return new JpaPagingItemReaderBuilder<FileEntity>()
                .name("fileReader")
                .entityManagerFactory(entityManagerFactory)
//                .queryString("SELECT f FROM FileEntity f WHERE f.isDeleted = 'Y' AND f.deletedTime < :sevenDaysAgo")
//                .parameterValues(Collections.singletonMap("sevenDaysAgo", LocalDateTime.now().minusDays(7)))
//                한달
                .queryString("SELECT f FROM FileEntity f WHERE f.isDeleted = 'Y' AND f.deletedTime < :oneMonthsAgo")
                .parameterValues(Collections.singletonMap("oneMonthsAgo", LocalDateTime.now().minusMonths(1)))
                .build();
    }

    @Bean
    public ItemProcessor<FileEntity, FileEntity> fileProcessor() {
        return file -> {
            try {
                // S3에서 파일 삭제
                s3Service.hardDeleteFileS3(file.getId());
                return null;
            } catch (Exception e) {
                // 로그 기록 후 이 항목 건너뛰기
                System.err.println("S3에서 파일 삭제 중 오류 발생: " + e.getMessage());
                return null;
            }
        };
    }

    @Bean
    public JpaItemWriter<FileEntity> fileWriter() {
        return new JpaItemWriterBuilder<FileEntity>()
                .entityManagerFactory(entityManagerFactory)
                .usePersist(false) //persist 사용하지 않고  merge 사용
                .build();
    }
}

