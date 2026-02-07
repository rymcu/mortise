package com.rymcu.mortise.member.service;

import com.mybatisflex.core.service.IService;
import com.rymcu.mortise.member.entity.ProductSku;

import java.util.List;

/**
 * Created on 2025/11/20 11:00.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.member.service
 */
public interface ProductSkuService extends IService<ProductSku> {

    /**
     * 根据产品ID查询SKU列表
     *
     * @param productId 产品ID
     * @return SKU列表
     */
    List<ProductSku> findByProductId(Long productId);
}
