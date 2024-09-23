package com.example.coconote.config.cleanUpConfig;

import com.example.coconote.api.canvas.entity.Canvas;
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
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableBatchProcessing
public class CanvasCleanupJobConfig {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Bean
    public Job canvasCleanupJob() {
        return new JobBuilder("canvasCleanupJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(findAndDeleteCanvasesStep())
                .build();
    }

    @Bean
    public Step findAndDeleteCanvasesStep() {
        return new StepBuilder("findAndDeleteCanvasesStep", jobRepository)
                .<Canvas, Canvas>chunk(10, transactionManager)
                .reader(canvasReader()) // 매번 새로운 Reader를 생성
                .processor(canvasProcessor())
                .writer(canvasWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<Canvas> canvasReader() {
        return new JpaPagingItemReaderBuilder<Canvas>()
                .name("canvasReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(10) // 페이지 사이즈 설정
                .queryString("SELECT c FROM Canvas c WHERE c.isDeleted = 'Y' AND c.deletedTime < :oneMinutesAgo")
                .parameterValues(Collections.singletonMap("oneMinutesAgo", LocalDateTime.now().minusMinutes(1)))
                .build();
    }

    @Bean
    public ItemProcessor<Canvas, Canvas> canvasProcessor() {
        return canvas -> {
            try {
                return canvas; // 삭제할 캔버스를 그대로 반환
            } catch (Exception e) {
                System.err.println("Error processing canvas deletion: " + e.getMessage());
                return null; // 오류 발생 시 이 항목 건너뛰기
            }
        };
    }

    @Bean
    public ItemWriter<Canvas> canvasWriter() {
        return items -> {
            try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
                entityManager.getTransaction().begin();

                // 삭제할 캔버스 ID 리스트 수집
                List<Long> canvasIds = items.getItems().stream()
                        .map(Canvas::getId)
                        .collect(Collectors.toList());

                // 자식 캔버스 먼저 삭제
                if (!canvasIds.isEmpty()) {
                    entityManager.createQuery("DELETE FROM Canvas c WHERE c.parentCanvas.id IN :ids")
                            .setParameter("ids", canvasIds)
                            .executeUpdate();
                }

                // 이제 부모 캔버스를 삭제
                if (!canvasIds.isEmpty()) {
                    entityManager.createQuery("DELETE FROM Canvas c WHERE c.id IN :ids")
                            .setParameter("ids", canvasIds)
                            .executeUpdate();
                }

                entityManager.getTransaction().commit();
            } catch (Exception e) {
                System.err.println("Error during canvas deletion: " + e.getMessage());
            }
        };
    }
}
