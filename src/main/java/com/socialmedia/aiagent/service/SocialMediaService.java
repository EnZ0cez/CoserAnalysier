package com.socialmedia.aiagent.service;

import com.socialmedia.aiagent.model.SocialMediaContent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SocialMediaService {
    
    /**
     * Get platform name
     */
    String getPlatform();
    
    /**
     * Fetch content from a blogger
     */
    Flux<SocialMediaContent> fetchBloggerContent(String bloggerIdentifier, int limit);
    
    /**
     * Get blogger information
     */
    Mono<String> getBloggerName(String bloggerIdentifier);
    
    /**
     * Validate blogger identifier
     */
    boolean isValidBloggerIdentifier(String bloggerIdentifier);
}