package com.constructionhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ConstructionHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConstructionHubApplication.class, args);
    }
}
