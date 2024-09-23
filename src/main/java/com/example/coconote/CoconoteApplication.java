package com.example.coconote;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CoconoteApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoconoteApplication.class, args);
    }

}
