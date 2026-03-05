package com.rymcu.mortise.product.dto;

/**
 * 产品列表查询条件
 *
 * @author ronger
 */
public record ProductQueryParam(
        /** 产品标题关键字 */
        String keyword,
        /** 产品类型编码 */
        String productType,
        /** 分类ID */
        Long categoryId,
        /** 状态：0-草稿, 1-上架, 2-下架, 3-停产 */
        Integer status,
        /** 是否推荐 */
        Boolean isFeatured
) {
}
