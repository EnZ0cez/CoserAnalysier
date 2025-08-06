package com.socialmedia.aiagent.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentAnalysisRequest {
    
    @NotBlank(message = "Platform is required")
    @Pattern(regexp = "bilibili|douyin|weibo", message = "Platform must be one of: bilibili, douyin, weibo")
    private String platform;
    
    @NotBlank(message = "Blogger name or URL is required")
    private String bloggerIdentifier; // Can be name or URL
    
    private Integer limit = 10; // Number of posts to analyze
    
    private Boolean includeAnalysis = true; // Whether to perform AI analysis
}