package com.socialmedia.aiagent.service;

import com.socialmedia.aiagent.config.AgentConfig;
import com.socialmedia.aiagent.model.SocialMediaContent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiAnalysisService {
    
    private final ChatClient chatClient;
    private final AgentConfig agentConfig;
    
    /**
     * Analyze individual social media content
     */
    public Mono<String> analyzeContent(SocialMediaContent content) {
        return Mono.fromCallable(() -> {
            try {
                String contentText = buildContentText(content);
                
                PromptTemplate template = new PromptTemplate(agentConfig.getAnalysisPrompt());
                Prompt prompt = template.create(Map.of("content", contentText));
                
                ChatResponse response = chatClient.call(prompt);
                return response.getResult().getOutput().getContent();
                
            } catch (Exception e) {
                log.error("Error analyzing content: {}", e.getMessage());
                return "Analysis failed: " + e.getMessage();
            }
        });
    }
    
    /**
     * Analyze multiple contents and provide overall insights
     */
    public Mono<String> analyzeBatch(List<SocialMediaContent> contents) {
        return Mono.fromCallable(() -> {
            try {
                String batchContent = contents.stream()
                        .map(this::buildContentText)
                        .collect(Collectors.joining("\n\n---\n\n"));
                
                String batchPrompt = """
                    You are an AI agent specialized in analyzing social media content from Chinese platforms.
                    Please analyze the following batch of content and provide comprehensive insights including:
                    
                    1. Overall content themes and trends
                    2. Sentiment distribution across posts
                    3. Popular hashtags and topics
                    4. Engagement patterns analysis
                    5. Content creator performance insights
                    6. Recommendations for content strategy
                    
                    Content batch to analyze:
                    {content}
                    
                    Please provide a structured analysis with clear sections and actionable insights.
                    """;
                
                PromptTemplate template = new PromptTemplate(batchPrompt);
                Prompt prompt = template.create(Map.of("content", 
                    batchContent.length() > agentConfig.getMaxContentLength() 
                        ? batchContent.substring(0, agentConfig.getMaxContentLength()) + "..." 
                        : batchContent));
                
                ChatResponse response = chatClient.call(prompt);
                return response.getResult().getOutput().getContent();
                
            } catch (Exception e) {
                log.error("Error analyzing batch content: {}", e.getMessage());
                return "Batch analysis failed: " + e.getMessage();
            }
        });
    }
    
    /**
     * Generate content recommendations based on analysis
     */
    public Mono<String> generateRecommendations(String platform, String bloggerName, List<SocialMediaContent> contents) {
        return Mono.fromCallable(() -> {
            try {
                String contentSummary = contents.stream()
                        .map(content -> String.format("Title: %s | Likes: %d | Comments: %d", 
                            content.getTitle(), 
                            content.getLikes() != null ? content.getLikes() : 0,
                            content.getComments() != null ? content.getComments() : 0))
                        .collect(Collectors.joining("\n"));
                
                String recommendationPrompt = """
                    You are an AI consultant specializing in social media strategy for Chinese platforms.
                    Based on the following content performance data, provide specific recommendations for the blogger.
                    
                    Platform: {platform}
                    Blogger: {blogger}
                    
                    Recent content performance:
                    {contentSummary}
                    
                    Please provide:
                    1. Content strategy recommendations
                    2. Optimal posting times and frequency
                    3. Trending topics to explore
                    4. Engagement improvement tactics
                    5. Platform-specific optimization tips
                    """;
                
                PromptTemplate template = new PromptTemplate(recommendationPrompt);
                Prompt prompt = template.create(Map.of(
                    "platform", platform,
                    "blogger", bloggerName,
                    "contentSummary", contentSummary));
                
                ChatResponse response = chatClient.call(prompt);
                return response.getResult().getOutput().getContent();
                
            } catch (Exception e) {
                log.error("Error generating recommendations: {}", e.getMessage());
                return "Recommendation generation failed: " + e.getMessage();
            }
        });
    }
    
    private String buildContentText(SocialMediaContent content) {
        StringBuilder text = new StringBuilder();
        text.append("Platform: ").append(content.getPlatform()).append("\n");
        text.append("Title: ").append(content.getTitle()).append("\n");
        
        if (content.getContent() != null && !content.getContent().isEmpty()) {
            text.append("Content: ").append(content.getContent()).append("\n");
        }
        
        text.append("Engagement: ");
        if (content.getLikes() != null) text.append("Likes: ").append(content.getLikes()).append(" ");
        if (content.getComments() != null) text.append("Comments: ").append(content.getComments()).append(" ");
        if (content.getShares() != null) text.append("Shares: ").append(content.getShares()).append(" ");
        if (content.getViews() != null) text.append("Views: ").append(content.getViews());
        
        return text.toString();
    }
}