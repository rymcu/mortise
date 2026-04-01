package com.rymcu.mortise.product.service.query.impl;

import com.rymcu.mortise.product.entity.ProductSku;
import com.rymcu.mortise.product.service.ProductSkuService;
import com.rymcu.mortise.product.service.query.ProductSkuQueryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductSkuQueryServiceImpl implements ProductSkuQueryService {

    private final ProductSkuService productSkuService;

    public ProductSkuQueryServiceImpl(ProductSkuService productSkuService) {
        this.productSkuService = productSkuService;
    }

    @Override
    public List<ProductSku> findByProductId(Long productId) {
        return productSkuService.findByProductId(productId);
    }

    @Override
    public ProductSku getById(Long skuId) {
        return productSkuService.getById(skuId);
    }
}
