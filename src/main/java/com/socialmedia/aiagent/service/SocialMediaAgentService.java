package com.socialmedia.aiagent.service;

import com.socialmedia.aiagent.model.SocialMediaContent;
import com.socialmedia.aiagent.model.dto.ContentAnalysisRequest;
import com.socialmedia.aiagent.model.dto.ContentAnalysisResponse;
import com.socialmedia.aiagent.repository.SocialMediaContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SocialMediaAgentService {
    
    private final Map<String, SocialMediaService> socialMediaServices;
    private final AiAnalysisService aiAnalysisService;
    private final SocialMediaContentRepository repository;
    
    /**
     * Main method to analyze blogger content across platforms
     */
    public Mono<ContentAnalysisResponse> analyzeBloggerContent(ContentAnalysisRequest request) {
        long startTime = System.currentTimeMillis();
        
        return validateRequest(request)
                .flatMap(validRequest -> {
                    SocialMediaService service = socialMediaServices.get(validRequest.getPlatform());
                    if (service == null) {
                        return Mono.error(new IllegalArgumentException("Unsupported platform: " + validRequest.getPlatform()));
                    }
                    
                    return fetchAndAnalyzeContent(service, validRequest)
                            .map(contents -> buildResponse(validRequest, contents, startTime));
                })
                .doOnSuccess(response -> log.info("Completed analysis for {} on {}: {} contents analyzed", 
                    request.getBloggerIdentifier(), request.getPlatform(), response.getTotalContents()))
                .doOnError(error -> log.error("Analysis failed for {} on {}: {}", 
                    request.getBloggerIdentifier(), request.getPlatform(), error.getMessage()));
    }
    
    /**
     * Get historical content for a blogger
     */
    public Mono<List<SocialMediaContent>> getHistoricalContent(String platform, String bloggerName) {
        return Mono.fromCallable(() -> 
            repository.findByPlatformAndBloggerNameOrderByPublishTimeDesc(platform, bloggerName))
            .subscribeOn(Schedulers.boundedElastic());
    }
    
    /**
     * Get recent content across all platforms
     */
    public Mono<List<SocialMediaContent>> getRecentContent(int hours) {
        return Mono.fromCallable(() -> 
            repository.findRecentContentByPlatform("", LocalDateTime.now().minusHours(hours)))
            .subscribeOn(Schedulers.boundedElastic());
    }
    
    private Mono<ContentAnalysisRequest> validateRequest(ContentAnalysisRequest request) {
        return Mono.fromCallable(() -> {
            SocialMediaService service = socialMediaServices.get(request.getPlatform());
            if (service == null) {
                throw new IllegalArgumentException("Unsupported platform: " + request.getPlatform());
            }
            
            if (!service.isValidBloggerIdentifier(request.getBloggerIdentifier())) {
                throw new IllegalArgumentException("Invalid blogger identifier for platform " + request.getPlatform());
            }
            
            return request;
        });
    }
    
    private Mono<List<SocialMediaContent>> fetchAndAnalyzeContent(SocialMediaService service, ContentAnalysisRequest request) {
        return service.fetchBloggerContent(request.getBloggerIdentifier(), request.getLimit())
                .flatMap(content -> {
                    if (request.getIncludeAnalysis()) {
                        return aiAnalysisService.analyzeContent(content)
                                .map(analysis -> {
                                    content.setAiAnalysis(analysis);
                                    return content;
                                })
                                .onErrorReturn(content); // Continue even if analysis fails
                    }
                    return Mono.just(content);
                })
                .collectList()
                .flatMap(contents -> saveContents(contents).thenReturn(contents));
    }
    
    private Mono<Void> saveContents(List<SocialMediaContent> contents) {
        return Mono.fromRunnable(() -> {
            try {
                for (SocialMediaContent content : contents) {
                    // Check if content already exists
                    if (content.getContentUrl() != null) {
                        var existing = repository.findByPlatformAndContentUrl(
                            content.getPlatform(), content.getContentUrl());
                        if (existing.isEmpty()) {
                            repository.save(content);
                        }
                    } else {
                        repository.save(content);
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to save some contents: {}", e.getMessage());
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
    
    private ContentAnalysisResponse buildResponse(ContentAnalysisRequest request, 
                                                  List<SocialMediaContent> contents, 
                                                  long startTime) {
        
        String overallAnalysis = "";
        if (request.getIncludeAnalysis() && !contents.isEmpty()) {
            try {
                overallAnalysis = aiAnalysisService.analyzeBatch(contents).block();
            } catch (Exception e) {
                log.warn("Failed to generate overall analysis: {}", e.getMessage());
                overallAnalysis = "Overall analysis failed: " + e.getMessage();
            }
        }
        
        String bloggerName = contents.isEmpty() ? "Unknown" : contents.get(0).getBloggerName();
        
        return ContentAnalysisResponse.builder()
                .platform(request.getPlatform())
                .bloggerName(bloggerName)
                .totalContents(contents.size())
                .contents(contents)
                .overallAnalysis(overallAnalysis)
                .processingTimeMs(System.currentTimeMillis() - startTime)
                .build();
    }
}