package com.rymcu.mortise.product.service;

import com.mybatisflex.core.service.IService;
import com.rymcu.mortise.product.entity.ProductSkuTarget;

import java.util.Collection;
import java.util.List;

/**
 * 产品 SKU 目标映射服务
 *
 * @author ronger
 */
public interface ProductSkuTargetService extends IService<ProductSkuTarget> {

    List<ProductSkuTarget> findByProductSkuId(Long productSkuId);

    List<ProductSkuTarget> findByProductSkuIds(Collection<Long> productSkuIds);
}
