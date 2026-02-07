package com.rymcu.mortise.member.service;

import com.mybatisflex.core.service.IService;
import com.rymcu.mortise.member.entity.ProductSkuTarget;

import java.util.List;

/**
 * Created on 2025/11/20 11:02.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.member.service
 */
public interface ProductSkuTargetService extends IService<ProductSkuTarget> {

    /**
     * 根据SKU ID查询目标关联列表
     *
     * @param skuId SKU ID
     * @return 目标关联列表
     */
    List<ProductSkuTarget> findBySkuId(Long skuId);

    /**
     * 根据多个SKU ID查询目标关联列表
     *
     * @param skuIds SKU ID列表
     * @return 目标关联列表
     */
    List<ProductSkuTarget> findBySkuIds(List<Long> skuIds);
}
