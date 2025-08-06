package com.socialmedia.aiagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AiSocialAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiSocialAgentApplication.class, args);
    }
}