package com.socialmedia.aiagent.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "social_media_content")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialMediaContent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String platform; // bilibili, douyin, weibo
    
    @Column(nullable = false)
    private String bloggerName;
    
    @Column(nullable = false)
    private String bloggerUrl;
    
    @Column(nullable = false, length = 500)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column
    private String contentUrl;
    
    @Column
    private Integer likes;
    
    @Column
    private Integer comments;
    
    @Column
    private Integer shares;
    
    @Column
    private Integer views;
    
    @Column(columnDefinition = "TEXT")
    private String aiAnalysis;
    
    @Column
    private LocalDateTime publishTime;
    
    @Column
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}