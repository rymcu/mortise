package com.rymcu.mortise.product.admin.facade;

import com.rymcu.mortise.product.entity.ProductSkuTarget;

import java.util.List;

public interface ProductSkuTargetAdminFacade {

    List<ProductSkuTarget> listTargets(Long productId, Long skuId);

    ProductSkuTarget getTarget(Long productId, Long skuId, Long targetId);

    ProductSkuTarget createTarget(Long productId, Long skuId, ProductSkuTarget target);

    boolean updateTarget(Long productId, Long skuId, Long targetId, ProductSkuTarget target);

    boolean deleteTarget(Long productId, Long skuId, Long targetId);
}
