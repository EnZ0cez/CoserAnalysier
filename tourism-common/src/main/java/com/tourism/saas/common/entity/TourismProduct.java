package com.tourism.saas.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 旅游产品实体类
 */
@Data
@TableName("t_tourism_product")
public class TourismProduct implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 产品ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 分类ID
     */
    @TableField("category_id")
    private Long categoryId;
    
    /**
     * 产品名称
     */
    @TableField("name")
    private String name;
    
    /**
     * 产品副标题
     */
    @TableField("subtitle")
    private String subtitle;
    
    /**
     * 产品描述
     */
    @TableField("description")
    private String description;
    
    /**
     * 产品详情HTML
     */
    @TableField("detail_html")
    private String detailHtml;
    
    /**
     * 主图片
     */
    @TableField("main_image")
    private String mainImage;
    
    /**
     * 子图片，JSON格式
     */
    @TableField("sub_images")
    private String subImages;
    
    /**
     * 原价
     */
    @TableField("original_price")
    private BigDecimal originalPrice;
    
    /**
     * 现价
     */
    @TableField("current_price")
    private BigDecimal currentPrice;
    
    /**
     * 库存数量
     */
    @TableField("stock")
    private Integer stock;
    
    /**
     * 销量
     */
    @TableField("sales_count")
    private Integer salesCount;
    
    /**
     * 浏览量
     */
    @TableField("view_count")
    private Integer viewCount;
    
    /**
     * 目的地
     */
    @TableField("destination")
    private String destination;
    
    /**
     * 行程天数
     */
    @TableField("duration_days")
    private Integer durationDays;
    
    /**
     * 出发城市
     */
    @TableField("departure_city")
    private String departureCity;
    
    /**
     * 旅游类型：1-跟团游，2-自由行，3-当地游，4-邮轮游
     */
    @TableField("travel_type")
    private Integer travelType;
    
    /**
     * 难度等级：1-轻松，2-一般，3-较难，4-困难
     */
    @TableField("difficulty_level")
    private Integer difficultyLevel;
    
    /**
     * 适合年龄范围
     */
    @TableField("age_range")
    private String ageRange;
    
    /**
     * 标签，逗号分隔
     */
    @TableField("tags")
    private String tags;
    
    /**
     * 产品特色
     */
    @TableField("features")
    private String features;
    
    /**
     * 包含服务
     */
    @TableField("included_services")
    private String includedServices;
    
    /**
     * 不包含服务
     */
    @TableField("excluded_services")
    private String excludedServices;
    
    /**
     * 预订须知
     */
    @TableField("booking_notice")
    private String bookingNotice;
    
    /**
     * 退改政策
     */
    @TableField("refund_policy")
    private String refundPolicy;
    
    /**
     * 状态：0-下架，1-上架，2-待审核
     */
    @TableField("status")
    private Integer status;
    
    /**
     * 排序序号
     */
    @TableField("sort_order")
    private Integer sortOrder;
    
    /**
     * SEO标题
     */
    @TableField("seo_title")
    private String seoTitle;
    
    /**
     * SEO关键词
     */
    @TableField("seo_keywords")
    private String seoKeywords;
    
    /**
     * SEO描述
     */
    @TableField("seo_description")
    private String seoDescription;
    
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 逻辑删除标志：0-未删除，1-已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}