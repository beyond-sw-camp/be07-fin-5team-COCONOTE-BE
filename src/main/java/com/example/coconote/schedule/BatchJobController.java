package com.example.coconote.schedule;


import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Locale;

@RestController
@RequestMapping("/api/batch")
public class BatchJobController {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job canvasCleanupJob;

    @PostMapping("/trigger")
    public ResponseEntity<String> triggerJob() {
        try {
            // Job 파라미터 설정
            jobLauncher.run(canvasCleanupJob,
                    new JobParametersBuilder()
                            .addDate("date", new Date())
                            .toJobParameters());

            return ResponseEntity.ok("Batch job triggered successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to trigger job: " + e.getMessage());
        }
    }
}
