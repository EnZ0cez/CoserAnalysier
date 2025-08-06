package com.socialmedia.aiagent.config;

import com.socialmedia.aiagent.service.SocialMediaService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class ServiceConfig {
    
    @Bean
    public Map<String, SocialMediaService> socialMediaServices(List<SocialMediaService> services) {
        return services.stream()
                .collect(Collectors.toMap(
                    SocialMediaService::getPlatform,
                    Function.identity()
                ));
    }
}