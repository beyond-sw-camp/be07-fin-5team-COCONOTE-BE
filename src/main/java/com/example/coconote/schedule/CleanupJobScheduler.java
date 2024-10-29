package com.example.coconote.schedule;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
public class CleanupJobScheduler {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job fileCleanupJob;

    @Autowired
    private Job threadCleanupJob;

    @Autowired
    private Job canvasCleanupJob;

//    @Scheduled(cron = "0 0 1 * * ?") // 매일 새벽 1시에 실행
    @Scheduled(cron = "0 0 1 * * ?") // 매일 새벽 1시에 실행
    public void runFileCleanupJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addDate("date", new Date())
                .toJobParameters();
        jobLauncher.run(fileCleanupJob, jobParameters);
    }
    @Scheduled(cron = "0 0 1 * * ?") // 매일 새벽 1시에 실행
    public void runThreadCleanupJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addDate("date", new Date())
                .toJobParameters();
        jobLauncher.run(threadCleanupJob, jobParameters);
    }

    @Scheduled(cron = "0 0 1 * * ?") // 매일 새벽 1시에 실행
    public void runCanvasCleanupJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addDate("date", new Date())
                .toJobParameters();
        jobLauncher.run(canvasCleanupJob, jobParameters);
    }
}
