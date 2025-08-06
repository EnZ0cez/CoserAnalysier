package com.socialmedia.aiagent.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialmedia.aiagent.config.SocialMediaConfig;
import com.socialmedia.aiagent.model.SocialMediaContent;
import com.socialmedia.aiagent.service.SocialMediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class BilibiliService implements SocialMediaService {
    
    private final WebClient webClient;
    private final SocialMediaConfig socialMediaConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final Pattern BILIBILI_USER_PATTERN = Pattern.compile("^(\\d+|space\\.bilibili\\.com/\\d+)$");
    
    @Override
    public String getPlatform() {
        return "bilibili";
    }
    
    @Override
    public Flux<SocialMediaContent> fetchBloggerContent(String bloggerIdentifier, int limit) {
        return getBloggerUID(bloggerIdentifier)
                .flatMapMany(uid -> fetchUserVideos(uid, limit))
                .onErrorResume(error -> {
                    log.error("Error fetching Bilibili content for {}: {}", bloggerIdentifier, error.getMessage());
                    return Flux.empty();
                });
    }
    
    @Override
    public Mono<String> getBloggerName(String bloggerIdentifier) {
        return getBloggerUID(bloggerIdentifier)
                .flatMap(this::fetchUserInfo)
                .map(userInfo -> userInfo.path("data").path("name").asText())
                .onErrorReturn("Unknown Bilibili User");
    }
    
    @Override
    public boolean isValidBloggerIdentifier(String bloggerIdentifier) {
        return BILIBILI_USER_PATTERN.matcher(bloggerIdentifier).matches();
    }
    
    private Mono<String> getBloggerUID(String bloggerIdentifier) {
        if (bloggerIdentifier.matches("\\d+")) {
            return Mono.just(bloggerIdentifier);
        }
        
        if (bloggerIdentifier.startsWith("space.bilibili.com/")) {
            String uid = bloggerIdentifier.substring(19);
            return Mono.just(uid);
        }
        
        return Mono.error(new IllegalArgumentException("Invalid Bilibili blogger identifier: " + bloggerIdentifier));
    }
    
    private Mono<JsonNode> fetchUserInfo(String uid) {
        String url = socialMediaConfig.getBilibili().getBaseUrl() + "/x/space/acc/info?mid=" + uid;
        
        return webClient.get()
                .uri(url)
                .header("User-Agent", socialMediaConfig.getBilibili().getUserAgent())
                .retrieve()
                .bodyToMono(String.class)
                .map(this::parseJson)
                .doOnNext(response -> log.debug("Bilibili user info response: {}", response))
                .onErrorResume(error -> {
                    log.warn("Failed to fetch Bilibili user info for UID {}: {}", uid, error.getMessage());
                    return Mono.just(objectMapper.createObjectNode());
                });
    }
    
    private Flux<SocialMediaContent> fetchUserVideos(String uid, int limit) {
        String url = socialMediaConfig.getBilibili().getBaseUrl() + "/x/space/arc/search?mid=" + uid + "&ps=" + Math.min(limit, 50);
        
        return webClient.get()
                .uri(url)
                .header("User-Agent", socialMediaConfig.getBilibili().getUserAgent())
                .retrieve()
                .bodyToMono(String.class)
                .map(this::parseJson)
                .flatMapMany(response -> {
                    JsonNode videos = response.path("data").path("list").path("vlist");
                    return Flux.fromIterable(videos)
                            .cast(JsonNode.class)
                            .take(limit)
                            .map(video -> convertToSocialMediaContent(video, uid));
                })
                .onErrorResume(error -> {
                    log.warn("Failed to fetch Bilibili videos for UID {}: {}", uid, error.getMessage());
                    return Flux.empty();
                });
    }
    
    private SocialMediaContent convertToSocialMediaContent(JsonNode video, String uid) {
        try {
            return SocialMediaContent.builder()
                    .platform(getPlatform())
                    .bloggerName(video.path("author").asText("Unknown"))
                    .bloggerUrl("https://space.bilibili.com/" + uid)
                    .title(video.path("title").asText())
                    .content(video.path("description").asText())
                    .contentUrl("https://www.bilibili.com/video/" + video.path("bvid").asText())
                    .views(video.path("play").asInt(0))
                    .likes(video.path("favorites").asInt(0))
                    .comments(video.path("comment").asInt(0))
                    .publishTime(LocalDateTime.ofInstant(
                            Instant.ofEpochSecond(video.path("created").asLong()),
                            ZoneId.systemDefault()))
                    .build();
        } catch (Exception e) {
            log.error("Error converting Bilibili video to SocialMediaContent: {}", e.getMessage());
            return SocialMediaContent.builder()
                    .platform(getPlatform())
                    .bloggerName("Unknown")
                    .bloggerUrl("https://space.bilibili.com/" + uid)
                    .title("Error parsing content")
                    .content("")
                    .build();
        }
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