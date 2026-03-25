package com.smartpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmartPAApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartPAApplication.class, args);
    }
}
