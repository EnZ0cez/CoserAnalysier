package com.tourism.saas.product.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 产品搜索请求DTO
 */
@Data
public class ProductSearchDTO {
    
    /**
     * 关键词
     */
    private String keyword;
    
    /**
     * 分类ID
     */
    private Long categoryId;
    
    /**
     * 目的地
     */
    private String destination;
    
    /**
     * 出发城市
     */
    private String departureCity;
    
    /**
     * 旅游类型：1-跟团游，2-自由行，3-当地游，4-邮轮游
     */
    private Integer travelType;
    
    /**
     * 最低价格
     */
    private BigDecimal minPrice;
    
    /**
     * 最高价格
     */
    private BigDecimal maxPrice;
    
    /**
     * 行程天数-最少
     */
    private Integer minDuration;
    
    /**
     * 行程天数-最多
     */
    private Integer maxDuration;
    
    /**
     * 排序方式：1-价格升序，2-价格降序，3-销量降序，4-评分降序，5-时间降序
     */
    private Integer sortType;
    
    /**
     * 页码
     */
    private Integer pageNum;
    
    /**
     * 每页大小
     */
    private Integer pageSize;
    
    public ProductSearchDTO() {
        this.pageNum = 1;
        this.pageSize = 20;
        this.sortType = 5; // 默认按时间降序
    }
}