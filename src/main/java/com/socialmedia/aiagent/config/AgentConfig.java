package com.socialmedia.aiagent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "agent")
@Data
public class AgentConfig {
    
    private Integer maxContentLength = 5000;
    private String analysisPrompt = """
        You are an AI agent specialized in analyzing social media content from Chinese platforms.
        Please analyze the following content and provide insights about:
        1. Content theme and main topics
        2. Sentiment analysis
        3. Popular trends or hashtags
        4. Engagement patterns
        5. Key insights for content creators
        
        Content to analyze: {content}
        """;
}