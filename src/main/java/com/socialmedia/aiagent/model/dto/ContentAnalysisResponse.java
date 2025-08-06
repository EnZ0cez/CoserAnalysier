package com.socialmedia.aiagent.model.dto;

import com.socialmedia.aiagent.model.SocialMediaContent;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentAnalysisResponse {
    
    private String platform;
    private String bloggerName;
    private Integer totalContents;
    private List<SocialMediaContent> contents;
    private String overallAnalysis;
    private Long processingTimeMs;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContentSummary {
        private String title;
        private String contentPreview;
        private Integer engagement; // likes + comments + shares
        private String sentiment;
        private String aiInsights;
    }
}