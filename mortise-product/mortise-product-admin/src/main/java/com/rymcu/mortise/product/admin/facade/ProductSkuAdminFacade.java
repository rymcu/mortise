package com.rymcu.mortise.product.admin.facade;

import com.rymcu.mortise.product.entity.ProductSku;

import java.util.List;

public interface ProductSkuAdminFacade {

    List<ProductSku> listSkus(Long productId);

    ProductSku getSku(Long skuId);

    ProductSku createSku(Long productId, ProductSku sku);

    boolean updateSku(Long productId, Long skuId, ProductSku sku);

    Boolean updateSkuStatus(Long skuId, String status);

    Boolean setDefaultSku(Long productId, Long skuId);

    boolean deleteSku(Long skuId);
}
