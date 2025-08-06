package com.socialmedia.aiagent.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialmedia.aiagent.config.SocialMediaConfig;
import com.socialmedia.aiagent.model.SocialMediaContent;
import com.socialmedia.aiagent.service.SocialMediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeiboService implements SocialMediaService {
    
    private final WebClient webClient;
    private final SocialMediaConfig socialMediaConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final Pattern WEIBO_USER_PATTERN = Pattern.compile("^(https?://)?(m\\.|www\\.)?weibo\\.(cn|com)/(u/)?([\\w\\-]+).*$");
    private static final Pattern WEIBO_UID_PATTERN = Pattern.compile("^\\d+$");
    
    @Override
    public String getPlatform() {
        return "weibo";
    }
    
    @Override
    public Flux<SocialMediaContent> fetchBloggerContent(String bloggerIdentifier, int limit) {
        return getUserId(bloggerIdentifier)
                .flatMapMany(userId -> fetchUserWeibos(userId, limit))
                .onErrorResume(error -> {
                    log.error("Error fetching Weibo content for {}: {}", bloggerIdentifier, error.getMessage());
                    return Flux.empty();
                });
    }
    
    @Override
    public Mono<String> getBloggerName(String bloggerIdentifier) {
        return getUserId(bloggerIdentifier)
                .flatMap(this::fetchUserInfo)
                .map(userInfo -> userInfo.path("data").path("userInfo").path("screen_name").asText())
                .onErrorReturn("Unknown Weibo User");
    }
    
    @Override
    public boolean isValidBloggerIdentifier(String bloggerIdentifier) {
        return WEIBO_USER_PATTERN.matcher(bloggerIdentifier).matches() ||
               WEIBO_UID_PATTERN.matcher(bloggerIdentifier).matches();
    }
    
    private Mono<String> getUserId(String bloggerIdentifier) {
        if (WEIBO_UID_PATTERN.matcher(bloggerIdentifier).matches()) {
            return Mono.just(bloggerIdentifier);
        }
        
        // Extract user ID from URL patterns
        if (WEIBO_USER_PATTERN.matcher(bloggerIdentifier).matches()) {
            String[] parts = bloggerIdentifier.split("/");
            for (int i = 0; i < parts.length; i++) {
                if ("u".equals(parts[i]) && i + 1 < parts.length) {
                    return Mono.just(parts[i + 1]);
                }
            }
            // If no /u/ pattern, try the last part
            String lastPart = parts[parts.length - 1];
            if (lastPart.matches("\\d+")) {
                return Mono.just(lastPart);
            }
        }
        
        return Mono.error(new IllegalArgumentException("Invalid Weibo blogger identifier: " + bloggerIdentifier));
    }
    
    private Mono<JsonNode> fetchUserInfo(String userId) {
        String url = socialMediaConfig.getWeibo().getBaseUrl() + "/api/container/getIndex?type=uid&value=" + userId;
        
        return webClient.get()
                .uri(url)
                .header("User-Agent", socialMediaConfig.getWeibo().getUserAgent())
                .header("Referer", "https://m.weibo.cn/")
                .retrieve()
                .bodyToMono(String.class)
                .map(this::parseJson)
                .doOnNext(response -> log.debug("Weibo user info response: {}", response))
                .onErrorResume(error -> {
                    log.warn("Failed to fetch Weibo user info for ID {}: {}", userId, error.getMessage());
                    return Mono.just(objectMapper.createObjectNode());
                });
    }
    
    private Flux<SocialMediaContent> fetchUserWeibos(String userId, int limit) {
        String url = socialMediaConfig.getWeibo().getBaseUrl() + "/api/container/getIndex?type=uid&value=" + userId;
        
        return webClient.get()
                .uri(url)
                .header("User-Agent", socialMediaConfig.getWeibo().getUserAgent())
                .header("Referer", "https://m.weibo.cn/")
                .retrieve()
                .bodyToMono(String.class)
                .map(this::parseJson)
                .flatMapMany(response -> {
                    JsonNode cards = response.path("data").path("cards");
                    return Flux.fromIterable(cards)
                            .cast(JsonNode.class)
                            .filter(card -> card.path("card_type").asInt() == 9) // Weibo posts
                            .take(limit)
                            .map(card -> convertToSocialMediaContent(card.path("mblog"), userId));
                })
                .onErrorResume(error -> {
                    log.warn("Failed to fetch Weibo posts for user ID {}: {}", userId, error.getMessage());
                    return Flux.empty();
                });
    }
    
    private SocialMediaContent convertToSocialMediaContent(JsonNode mblog, String userId) {
        try {
            // Parse publish time
            LocalDateTime publishTime = LocalDateTime.now();
            try {
                String createdAt = mblog.path("created_at").asText();
                if (!createdAt.isEmpty()) {
                    // Weibo time format: "Mon Mar 20 14:30:00 +0800 2023"
                    // This is a simplified parsing - real implementation would need proper date parsing
                    publishTime = LocalDateTime.now().minusHours(1); // Placeholder
                }
            } catch (Exception e) {
                log.debug("Could not parse Weibo publish time: {}", e.getMessage());
            }
            
            // Clean HTML content
            String rawText = mblog.path("text").asText();
            String cleanText = cleanHtmlContent(rawText);
            
            return SocialMediaContent.builder()
                    .platform(getPlatform())
                    .bloggerName(mblog.path("user").path("screen_name").asText("Unknown"))
                    .bloggerUrl("https://m.weibo.cn/u/" + userId)
                    .title(truncateTitle(cleanText))
                    .content(cleanText)
                    .contentUrl("https://m.weibo.cn/status/" + mblog.path("id").asText())
                    .likes(mblog.path("attitudes_count").asInt(0))
                    .comments(mblog.path("comments_count").asInt(0))
                    .shares(mblog.path("reposts_count").asInt(0))
                    .publishTime(publishTime)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error converting Weibo post to SocialMediaContent: {}", e.getMessage());
            return SocialMediaContent.builder()
                    .platform(getPlatform())
                    .bloggerName("Unknown")
                    .bloggerUrl("https://m.weibo.cn/u/" + userId)
                    .title("Error parsing content")
                    .content("")
                    .build();
        }
    }
    
    private String cleanHtmlContent(String htmlContent) {
        try {
            Document doc = Jsoup.parse(htmlContent);
            return doc.text();
        } catch (Exception e) {
            return htmlContent;
        }
    }
    
    private String truncateTitle(String content) {
        if (content.length() <= 100) {
            return content;
        }
        return content.substring(0, 97) + "...";
    }
    
    private JsonNode parseJson(String jsonString) {
        try {
            return objectMapper.readTree(jsonString);
        } catch (Exception e) {
            log.error("Failed to parse JSON: {}", e.getMessage());
            return objectMapper.createObjectNode();
        }
    }
}