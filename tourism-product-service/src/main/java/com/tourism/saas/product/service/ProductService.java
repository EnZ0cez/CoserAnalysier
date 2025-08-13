package com.tourism.saas.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tourism.saas.common.entity.TourismProduct;
import com.tourism.saas.common.result.Result;
import com.tourism.saas.product.dto.ProductSearchDTO;
import com.tourism.saas.product.vo.ProductVO;

import java.util.List;

/**
 * 产品服务接口
 */
public interface ProductService {
    
    /**
     * 分页搜索产品
     */
    Result<IPage<ProductVO>> searchProducts(ProductSearchDTO searchDTO);
    
    /**
     * 根据ID获取产品详情
     */
    Result<ProductVO> getProductById(Long productId);
    
    /**
     * 获取热门产品
     */
    Result<List<ProductVO>> getHotProducts(Integer limit);
    
    /**
     * 获取推荐产品
     */
    Result<List<ProductVO>> getRecommendedProducts(Integer type, Integer limit);
    
    /**
     * 获取相关产品
     */
    Result<List<ProductVO>> getRelatedProducts(Long productId, Integer limit);
    
    /**
     * 增加产品浏览量
     */
    void incrementViewCount(Long productId);
    
    /**
     * 智能产品推荐（基于用户行为）
     */
    Result<List<ProductVO>> getSmartRecommendations(Long userId, Integer limit);
    
    /**
     * 获取产品统计信息
     */
    Result<Object> getProductStatistics(Long categoryId);
    
    /**
     * 转换产品实体为VO
     */
    ProductVO convertToVO(TourismProduct product);
    
    /**
     * 批量转换产品实体为VO
     */
    List<ProductVO> convertToVOList(List<TourismProduct> products);
}