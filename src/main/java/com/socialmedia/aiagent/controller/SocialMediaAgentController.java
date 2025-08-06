package com.socialmedia.aiagent.controller;

import com.socialmedia.aiagent.model.SocialMediaContent;
import com.socialmedia.aiagent.model.dto.ContentAnalysisRequest;
import com.socialmedia.aiagent.model.dto.ContentAnalysisResponse;
import com.socialmedia.aiagent.service.AiAnalysisService;
import com.socialmedia.aiagent.service.SocialMediaAgentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/agent")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SocialMediaAgentController {
    
    private final SocialMediaAgentService agentService;
    private final AiAnalysisService aiAnalysisService;
    
    /**
     * Analyze blogger content from social media platforms
     */
    @PostMapping("/analyze")
    public Mono<ResponseEntity<ContentAnalysisResponse>> analyzeBloggerContent(
            @Valid @RequestBody ContentAnalysisRequest request) {
        
        log.info("Analyzing content for blogger: {} on platform: {}", 
            request.getBloggerIdentifier(), request.getPlatform());
        
        return agentService.analyzeBloggerContent(request)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> {
                    log.error("Analysis failed: {}", error.getMessage());
                    return Mono.just(ResponseEntity.badRequest()
                            .body(ContentAnalysisResponse.builder()
                                    .platform(request.getPlatform())
                                    .bloggerName("Error")
                                    .totalContents(0)
                                    .overallAnalysis("Analysis failed: " + error.getMessage())
                                    .build()));
                });
    }
    
    /**
     * Get recommendations for a blogger based on their content
     */
    @PostMapping("/recommendations")
    public Mono<ResponseEntity<Map<String, String>>> getRecommendations(
            @RequestParam String platform,
            @RequestParam String bloggerName) {
        
        return agentService.getHistoricalContent(platform, bloggerName)
                .flatMap(contents -> {
                    if (contents.isEmpty()) {
                        return Mono.just(Map.of("error", "No content found for blogger"));
                    }
                    return aiAnalysisService.generateRecommendations(platform, bloggerName, contents)
                            .map(recommendations -> Map.of("recommendations", recommendations));
                })
                .map(ResponseEntity::ok)
                .onErrorResume(error -> 
                    Mono.just(ResponseEntity.badRequest()
                            .body(Map.of("error", error.getMessage()))));
    }
    
    /**
     * Get historical content for a blogger
     */
    @GetMapping("/history")
    public Mono<ResponseEntity<List<SocialMediaContent>>> getHistoricalContent(
            @RequestParam String platform,
            @RequestParam String bloggerName) {
        
        return agentService.getHistoricalContent(platform, bloggerName)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> 
                    Mono.just(ResponseEntity.badRequest().build()));
    }
    
    /**
     * Get recent content across all platforms
     */
    @GetMapping("/recent")
    public Mono<ResponseEntity<List<SocialMediaContent>>> getRecentContent(
            @RequestParam(defaultValue = "24") int hours) {
        
        return agentService.getRecentContent(hours)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> 
                    Mono.just(ResponseEntity.badRequest().build()));
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "AI Social Media Agent",
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
    
    /**
     * Get supported platforms
     */
    @GetMapping("/platforms")
    public ResponseEntity<Map<String, Object>> getSupportedPlatforms() {
        return ResponseEntity.ok(Map.of(
                "platforms", List.of("bilibili", "douyin", "weibo"),
                "description", Map.of(
                        "bilibili", "Video platform - provide user ID or space.bilibili.com/[ID]",
                        "douyin", "Short video platform - provide user URL or @username",
                        "weibo", "Microblogging platform - provide user ID or profile URL"
                )
        ));
    }
}