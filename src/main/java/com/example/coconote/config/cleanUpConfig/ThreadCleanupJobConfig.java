package com.example.coconote.config.cleanUpConfig;

import com.example.coconote.api.thread.thread.entity.Thread;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.batch.core.launch.support.RunIdIncrementer;


import java.time.LocalDateTime;
import java.util.Collections;

@Configuration
@EnableBatchProcessing
public class ThreadCleanupJobConfig {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Bean
    public Job threadCleanupJob() {
        return new JobBuilder("threadCleanupJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(findAndDeleteThreadsStep())
                .build();
    }

    @Bean
    public Step findAndDeleteThreadsStep() {
        return new StepBuilder("findAndDeleteThreadsStep", jobRepository)
                .<Thread,Thread>chunk(10, transactionManager)
                .reader(threadReader())
                .processor(threadProcessor())
                .writer(threadWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<Thread> threadReader() {
        return new JpaPagingItemReaderBuilder<Thread>()
                .name("threadReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT t FROM Thread t WHERE t.isDeleted = 'Y' AND t.deletedTime < :oneMonthAgo")
                .parameterValues(Collections.singletonMap("oneMonthAgo", LocalDateTime.now().minusMonths(1)))
                .build();
    }

    @Bean
    public ItemProcessor<Thread, Thread> threadProcessor() {
        return thread -> {
            try {
                return thread;
            } catch (Exception e) {
                System.err.println("Error processing thread deletion: " + e.getMessage());
                return null; // skip this item on error
            }
        };
    }

    @Bean
    public ItemWriter<Thread> threadWriter() {
        return items -> {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            entityManager.getTransaction().begin();
            try {
                for (Thread thread : items) {
                    // 스레드 삭제
                    entityManager.remove(entityManager.contains(thread) ? thread : entityManager.merge(thread));
                }
                entityManager.getTransaction().commit();
            } catch (Exception e) {
                entityManager.getTransaction().rollback();
                System.err.println("Error during thread deletion: " + e.getMessage());
            } finally {
                entityManager.close();
            }
        };
    }
}