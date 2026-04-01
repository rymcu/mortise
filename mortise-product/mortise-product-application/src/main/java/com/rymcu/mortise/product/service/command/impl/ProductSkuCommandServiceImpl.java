package com.rymcu.mortise.product.service.command.impl;

import com.rymcu.mortise.product.entity.ProductSku;
import com.rymcu.mortise.product.service.ProductSkuService;
import com.rymcu.mortise.product.service.command.ProductSkuCommandService;
import org.springframework.stereotype.Service;

@Service
public class ProductSkuCommandServiceImpl implements ProductSkuCommandService {

    private final ProductSkuService productSkuService;

    public ProductSkuCommandServiceImpl(ProductSkuService productSkuService) {
        this.productSkuService = productSkuService;
    }

    @Override
    public boolean createSku(ProductSku sku) {
        return productSkuService.save(sku);
    }

    @Override
    public boolean updateSku(ProductSku sku) {
        return productSkuService.updateById(sku);
    }

    @Override
    public Boolean updateStatus(Long id, String status) {
        return productSkuService.updateStatus(id, status);
    }

    @Override
    public Boolean setDefault(Long productId, Long skuId) {
        return productSkuService.setDefault(productId, skuId);
    }

    @Override
    public boolean deleteSku(Long skuId) {
        return productSkuService.removeById(skuId);
    }
}
