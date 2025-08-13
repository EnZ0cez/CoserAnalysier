package com.tourism.saas.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.saas.common.entity.TourismProduct;
import com.tourism.saas.product.dto.ProductSearchDTO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 旅游产品Mapper接口
 */
@Mapper
public interface TourismProductMapper extends BaseMapper<TourismProduct> {
    
    /**
     * 分页搜索产品（优化的动态SQL查询）
     */
    @Select({
        "<script>",
        "SELECT * FROM t_tourism_product",
        "WHERE deleted = 0 AND status = 1",
        "<if test='searchDTO.categoryId != null'>",
        "  AND category_id = #{searchDTO.categoryId}",
        "</if>",
        "<if test='searchDTO.destination != null and searchDTO.destination != \"\"'>",
        "  AND destination LIKE CONCAT('%', #{searchDTO.destination}, '%')",
        "</if>",
        "<if test='searchDTO.departureCity != null and searchDTO.departureCity != \"\"'>",
        "  AND departure_city = #{searchDTO.departureCity}",
        "</if>",
        "<if test='searchDTO.travelType != null'>",
        "  AND travel_type = #{searchDTO.travelType}",
        "</if>",
        "<if test='searchDTO.minPrice != null'>",
        "  AND current_price >= #{searchDTO.minPrice}",
        "</if>",
        "<if test='searchDTO.maxPrice != null'>",
        "  AND current_price <= #{searchDTO.maxPrice}",
        "</if>",
        "<if test='searchDTO.minDuration != null'>",
        "  AND duration_days >= #{searchDTO.minDuration}",
        "</if>",
        "<if test='searchDTO.maxDuration != null'>",
        "  AND duration_days <= #{searchDTO.maxDuration}",
        "</if>",
        "<if test='searchDTO.keyword != null and searchDTO.keyword != \"\"'>",
        "  AND (name LIKE CONCAT('%', #{searchDTO.keyword}, '%')",
        "       OR destination LIKE CONCAT('%', #{searchDTO.keyword}, '%')",
        "       OR tags LIKE CONCAT('%', #{searchDTO.keyword}, '%'))",
        "</if>",
        "<choose>",
        "  <when test='searchDTO.sortType == 1'>ORDER BY current_price ASC</when>",
        "  <when test='searchDTO.sortType == 2'>ORDER BY current_price DESC</when>",
        "  <when test='searchDTO.sortType == 3'>ORDER BY sales_count DESC</when>",
        "  <when test='searchDTO.sortType == 4'>ORDER BY view_count DESC</when>",
        "  <otherwise>ORDER BY create_time DESC</otherwise>",
        "</choose>",
        "</script>"
    })
    IPage<TourismProduct> searchProducts(Page<TourismProduct> page, @Param("searchDTO") ProductSearchDTO searchDTO);
    
    /**
     * 获取热门产品（基于销量和浏览量）
     */
    @Select("SELECT * FROM t_tourism_product " +
            "WHERE deleted = 0 AND status = 1 " +
            "ORDER BY (sales_count * 0.7 + view_count * 0.3) DESC " +
            "LIMIT #{limit}")
    List<TourismProduct> getHotProducts(@Param("limit") Integer limit);
    
    /**
     * 获取推荐产品
     */
    @Select("SELECT p.* FROM t_tourism_product p " +
            "INNER JOIN t_product_recommendation r ON p.id = r.product_id " +
            "WHERE p.deleted = 0 AND p.status = 1 " +
            "AND r.deleted = 0 AND r.status = 1 " +
            "AND r.recommendation_type = #{type} " +
            "AND (r.start_time IS NULL OR r.start_time <= NOW()) " +
            "AND (r.end_time IS NULL OR r.end_time >= NOW()) " +
            "ORDER BY r.weight DESC, r.position ASC " +
            "LIMIT #{limit}")
    List<TourismProduct> getRecommendedProducts(@Param("type") Integer type, @Param("limit") Integer limit);
    
    /**
     * 根据目的地获取相关产品
     */
    @Select("SELECT * FROM t_tourism_product " +
            "WHERE deleted = 0 AND status = 1 " +
            "AND destination = #{destination} AND id != #{excludeId} " +
            "ORDER BY sales_count DESC " +
            "LIMIT #{limit}")
    List<TourismProduct> getRelatedProductsByDestination(@Param("destination") String destination, 
                                                        @Param("excludeId") Long excludeId, 
                                                        @Param("limit") Integer limit);
    
    /**
     * 增加浏览量
     */
    @Update("UPDATE t_tourism_product SET view_count = view_count + 1, update_time = NOW() " +
            "WHERE id = #{productId} AND deleted = 0")
    int incrementViewCount(@Param("productId") Long productId);
    
    /**
     * 批量更新产品状态
     */
    @Update("UPDATE t_tourism_product SET status = #{status}, update_time = NOW() " +
            "WHERE id IN (${productIds}) AND deleted = 0")
    int batchUpdateStatus(@Param("productIds") String productIds, @Param("status") Integer status);
    
    /**
     * 获取产品统计信息
     */
    @Select({
        "<script>",
        "SELECT",
        "  COUNT(*) as total_count,",
        "  SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as online_count,",
        "  SUM(sales_count) as total_sales,",
        "  SUM(view_count) as total_views,",
        "  AVG(current_price) as avg_price",
        "FROM t_tourism_product",
        "WHERE deleted = 0",
        "<if test='categoryId != null'>",
        "  AND category_id = #{categoryId}",
        "</if>",
        "</script>"
    })
    @Results({
        @Result(column = "total_count", property = "totalCount"),
        @Result(column = "online_count", property = "onlineCount"),
        @Result(column = "total_sales", property = "totalSales"),
        @Result(column = "total_views", property = "totalViews"),
        @Result(column = "avg_price", property = "avgPrice")
    })
    ProductStatisticsVO getProductStatistics(@Param("categoryId") Long categoryId);
    
    /**
     * 产品统计VO
     */
    class ProductStatisticsVO {
        private Long totalCount;
        private Long onlineCount;
        private Long totalSales;
        private Long totalViews;
        private Double avgPrice;
        
        // getters and setters
        public Long getTotalCount() { return totalCount; }
        public void setTotalCount(Long totalCount) { this.totalCount = totalCount; }
        public Long getOnlineCount() { return onlineCount; }
        public void setOnlineCount(Long onlineCount) { this.onlineCount = onlineCount; }
        public Long getTotalSales() { return totalSales; }
        public void setTotalSales(Long totalSales) { this.totalSales = totalSales; }
        public Long getTotalViews() { return totalViews; }
        public void setTotalViews(Long totalViews) { this.totalViews = totalViews; }
        public Double getAvgPrice() { return avgPrice; }
        public void setAvgPrice(Double avgPrice) { this.avgPrice = avgPrice; }
    }
}