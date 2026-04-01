package com.rymcu.mortise.product.service.query;

import com.rymcu.mortise.product.entity.ProductSku;

import java.util.List;

public interface ProductSkuQueryService {

    List<ProductSku> findByProductId(Long productId);

    ProductSku getById(Long skuId);
}
