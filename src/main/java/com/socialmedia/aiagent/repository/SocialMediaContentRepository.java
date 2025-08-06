package com.socialmedia.aiagent.repository;

import com.socialmedia.aiagent.model.SocialMediaContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SocialMediaContentRepository extends JpaRepository<SocialMediaContent, Long> {
    
    List<SocialMediaContent> findByPlatformAndBloggerNameOrderByPublishTimeDesc(String platform, String bloggerName);
    
    List<SocialMediaContent> findByPlatformOrderByPublishTimeDesc(String platform);
    
    Optional<SocialMediaContent> findByPlatformAndContentUrl(String platform, String contentUrl);
    
    @Query("SELECT c FROM SocialMediaContent c WHERE c.platform = :platform AND c.publishTime >= :since ORDER BY c.publishTime DESC")
    List<SocialMediaContent> findRecentContentByPlatform(@Param("platform") String platform, @Param("since") LocalDateTime since);
    
    @Query("SELECT c FROM SocialMediaContent c WHERE c.bloggerName = :bloggerName AND c.publishTime >= :since ORDER BY c.publishTime DESC")
    List<SocialMediaContent> findRecentContentByBlogger(@Param("bloggerName") String bloggerName, @Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(c) FROM SocialMediaContent c WHERE c.platform = :platform AND c.bloggerName = :bloggerName")
    Long countByPlatformAndBloggerName(@Param("platform") String platform, @Param("bloggerName") String bloggerName);
}