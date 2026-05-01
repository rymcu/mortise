package com.rymcu.mortise.product.service.query;

import com.rymcu.mortise.product.entity.ProductSkuTarget;

import java.util.Collection;
import java.util.List;

public interface ProductSkuTargetQueryService {

    List<ProductSkuTarget> findByProductSkuId(Long productSkuId);

    List<ProductSkuTarget> findByProductSkuIds(Collection<Long> productSkuIds);

    ProductSkuTarget getById(Long id);
}
