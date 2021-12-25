package com.possible.scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class EmailSchedulerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmailSchedulerApplication.class, args);
    }

}
