package com.tourism.saas.product.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 产品展示VO
 */
@Data
public class ProductVO {
    
    /**
     * 产品ID
     */
    private Long id;
    
    /**
     * 产品名称
     */
    private String name;
    
    /**
     * 产品副标题
     */
    private String subtitle;
    
    /**
     * 产品描述
     */
    private String description;
    
    /**
     * 主图片
     */
    private String mainImage;
    
    /**
     * 子图片列表
     */
    private List<String> subImages;
    
    /**
     * 原价
     */
    private BigDecimal originalPrice;
    
    /**
     * 现价
     */
    private BigDecimal currentPrice;
    
    /**
     * 库存数量
     */
    private Integer stock;
    
    /**
     * 销量
     */
    private Integer salesCount;
    
    /**
     * 浏览量
     */
    private Integer viewCount;
    
    /**
     * 目的地
     */
    private String destination;
    
    /**
     * 行程天数
     */
    private Integer durationDays;
    
    /**
     * 出发城市
     */
    private String departureCity;
    
    /**
     * 旅游类型：1-跟团游，2-自由行，3-当地游，4-邮轮游
     */
    private Integer travelType;
    
    /**
     * 旅游类型名称
     */
    private String travelTypeName;
    
    /**
     * 难度等级：1-轻松，2-一般，3-较难，4-困难
     */
    private Integer difficultyLevel;
    
    /**
     * 适合年龄范围
     */
    private String ageRange;
    
    /**
     * 标签列表
     */
    private List<String> tags;
    
    /**
     * 产品特色
     */
    private String features;
    
    /**
     * 是否推荐
     */
    private Boolean recommended;
    
    /**
     * 折扣百分比
     */
    private Integer discountPercent;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 计算折扣百分比
     */
    public Integer getDiscountPercent() {
        if (originalPrice != null && currentPrice != null && 
            originalPrice.compareTo(BigDecimal.ZERO) > 0 && 
            currentPrice.compareTo(originalPrice) < 0) {
            BigDecimal discount = originalPrice.subtract(currentPrice);
            return discount.multiply(BigDecimal.valueOf(100))
                    .divide(originalPrice, 0, BigDecimal.ROUND_HALF_UP)
                    .intValue();
        }
        return 0;
    }
    
    /**
     * 获取旅游类型名称
     */
    public String getTravelTypeName() {
        if (travelType == null) {
            return "未知";
        }
        switch (travelType) {
            case 1: return "跟团游";
            case 2: return "自由行";
            case 3: return "当地游";
            case 4: return "邮轮游";
            default: return "未知";
        }
    }
}