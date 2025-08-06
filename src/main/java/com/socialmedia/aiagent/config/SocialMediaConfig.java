package com.socialmedia.aiagent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "social-media")
@Data
public class SocialMediaConfig {
    
    private Bilibili bilibili = new Bilibili();
    private Douyin douyin = new Douyin();
    private Weibo weibo = new Weibo();
    
    @Data
    public static class Bilibili {
        private String baseUrl = "https://api.bilibili.com";
        private String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    }
    
    @Data
    public static class Douyin {
        private String baseUrl = "https://www.douyin.com";
        private String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    }
    
    @Data
    public static class Weibo {
        private String baseUrl = "https://m.weibo.cn";
        private String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    }
}