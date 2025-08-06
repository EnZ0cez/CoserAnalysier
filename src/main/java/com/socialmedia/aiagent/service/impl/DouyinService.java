package com.socialmedia.aiagent.service.impl;

import com.socialmedia.aiagent.config.SocialMediaConfig;
import com.socialmedia.aiagent.model.SocialMediaContent;
import com.socialmedia.aiagent.service.SocialMediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class DouyinService implements SocialMediaService {
    
    private final WebClient webClient;
    private final SocialMediaConfig socialMediaConfig;
    
    private static final Pattern DOUYIN_USER_PATTERN = Pattern.compile("^(https?://)?(www\\.)?douyin\\.com/user/([\\w\\-]+).*$");
    private static final Pattern DOUYIN_SHORT_PATTERN = Pattern.compile("^@([\\w\\-]+)$");
    
    @Override
    public String getPlatform() {
        return "douyin";
    }
    
    @Override
    public Flux<SocialMediaContent> fetchBloggerContent(String bloggerIdentifier, int limit) {
        return getUserUrl(bloggerIdentifier)
                .flatMapMany(userUrl -> fetchUserPage(userUrl, limit))
                .onErrorResume(error -> {
                    log.error("Error fetching Douyin content for {}: {}", bloggerIdentifier, error.getMessage());
                    return Flux.empty();
                });
    }
    
    @Override
    public Mono<String> getBloggerName(String bloggerIdentifier) {
        return getUserUrl(bloggerIdentifier)
                .flatMap(this::extractBloggerName)
                .onErrorReturn("Unknown Douyin User");
    }
    
    @Override
    public boolean isValidBloggerIdentifier(String bloggerIdentifier) {
        return DOUYIN_USER_PATTERN.matcher(bloggerIdentifier).matches() ||
               DOUYIN_SHORT_PATTERN.matcher(bloggerIdentifier).matches();
    }
    
    private Mono<String> getUserUrl(String bloggerIdentifier) {
        if (DOUYIN_USER_PATTERN.matcher(bloggerIdentifier).matches()) {
            return Mono.just(bloggerIdentifier.startsWith("http") ? bloggerIdentifier : "https://" + bloggerIdentifier);
        }
        
        if (DOUYIN_SHORT_PATTERN.matcher(bloggerIdentifier).matches()) {
            String username = bloggerIdentifier.substring(1);
            return Mono.just("https://www.douyin.com/user/" + username);
        }
        
        return Mono.error(new IllegalArgumentException("Invalid Douyin blogger identifier: " + bloggerIdentifier));
    }
    
    private Mono<String> extractBloggerName(String userUrl) {
        return webClient.get()
                .uri(userUrl)
                .header("User-Agent", socialMediaConfig.getDouyin().getUserAgent())
                .retrieve()
                .bodyToMono(String.class)
                .map(html -> {
                    try {
                        Document doc = Jsoup.parse(html);
                        Element titleElement = doc.selectFirst("title");
                        if (titleElement != null) {
                            String title = titleElement.text();
                            // Extract name from title pattern like "用户名 - 抖音"
                            if (title.contains(" - 抖音")) {
                                return title.split(" - 抖音")[0];
                            }
                        }
                        return "Unknown Douyin User";
                    } catch (Exception e) {
                        log.error("Error parsing Douyin user page: {}", e.getMessage());
                        return "Unknown Douyin User";
                    }
                });
    }
    
    private Flux<SocialMediaContent> fetchUserPage(String userUrl, int limit) {
        return webClient.get()
                .uri(userUrl)
                .header("User-Agent", socialMediaConfig.getDouyin().getUserAgent())
                .retrieve()
                .bodyToMono(String.class)
                .flatMapMany(html -> parseUserContent(html, userUrl, limit))
                .onErrorResume(error -> {
                    log.warn("Failed to fetch Douyin user page {}: {}", userUrl, error.getMessage());
                    return Flux.empty();
                });
    }
    
    private Flux<SocialMediaContent> parseUserContent(String html, String userUrl, int limit) {
        try {
            Document doc = Jsoup.parse(html);
            
            // Extract blogger name
            String bloggerName = "Unknown Douyin User";
            Element titleElement = doc.selectFirst("title");
            if (titleElement != null) {
                String title = titleElement.text();
                if (title.contains(" - 抖音")) {
                    bloggerName = title.split(" - 抖音")[0];
                }
            }
            
            // Note: Douyin's content is heavily JavaScript-rendered
            // This is a simplified approach that attempts to extract basic information
            // For production use, you would need to use a JavaScript-capable browser automation tool
            
            Elements videoElements = doc.select("a[href*='/video/']");
            final String finalBloggerName = bloggerName;
            
            return Flux.fromIterable(videoElements)
                    .take(limit)
                    .map(element -> createDouyinContent(element, finalBloggerName, userUrl))
                    .onErrorResume(error -> {
                        log.warn("Error parsing Douyin content: {}", error.getMessage());
                        return Flux.empty();
                    });
            
        } catch (Exception e) {
            log.error("Error parsing Douyin HTML: {}", e.getMessage());
            return Flux.empty();
        }
    }
    
    private SocialMediaContent createDouyinContent(Element element, String bloggerName, String userUrl) {
        try {
            String href = element.attr("href");
            String videoUrl = href.startsWith("http") ? href : "https://www.douyin.com" + href;
            
            // Extract title from element text or data attributes
            String title = element.text();
            if (title.isEmpty()) {
                title = element.attr("title");
            }
            if (title.isEmpty()) {
                title = "Douyin Video";
            }
            
            return SocialMediaContent.builder()
                    .platform(getPlatform())
                    .bloggerName(bloggerName)
                    .bloggerUrl(userUrl)
                    .title(title)
                    .content("") // Content extraction would require JavaScript execution
                    .contentUrl(videoUrl)
                    .publishTime(LocalDateTime.now()) // Actual timestamp would need API access
                    .build();
            
        } catch (Exception e) {
            log.error("Error creating Douyin content from element: {}", e.getMessage());
            return SocialMediaContent.builder()
                    .platform(getPlatform())
                    .bloggerName(bloggerName)
                    .bloggerUrl(userUrl)
                    .title("Error parsing content")
                    .content("")
                    .build();
        }
    }
}