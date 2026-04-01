package com.rymcu.mortise.product.service.command;

import com.rymcu.mortise.product.entity.ProductSku;

public interface ProductSkuCommandService {

    boolean createSku(ProductSku sku);

    boolean updateSku(ProductSku sku);

    Boolean updateStatus(Long id, String status);

    Boolean setDefault(Long productId, Long skuId);

    boolean deleteSku(Long skuId);
}
