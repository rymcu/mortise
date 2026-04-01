package com.rymcu.mortise.product.admin.facade.impl;

import com.rymcu.mortise.product.admin.facade.ProductSkuAdminFacade;
import com.rymcu.mortise.product.entity.ProductSku;
import com.rymcu.mortise.product.service.command.ProductSkuCommandService;
import com.rymcu.mortise.product.service.query.ProductSkuQueryService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductSkuAdminFacadeImpl implements ProductSkuAdminFacade {

    private final ProductSkuCommandService productSkuCommandService;
    private final ProductSkuQueryService productSkuQueryService;

    public ProductSkuAdminFacadeImpl(ProductSkuCommandService productSkuCommandService,
                                     ProductSkuQueryService productSkuQueryService) {
        this.productSkuCommandService = productSkuCommandService;
        this.productSkuQueryService = productSkuQueryService;
    }

    @Override
    public List<ProductSku> listSkus(Long productId) {
        return productSkuQueryService.findByProductId(productId);
    }

    @Override
    public ProductSku getSku(Long skuId) {
        return productSkuQueryService.getById(skuId);
    }

    @Override
    public ProductSku createSku(Long productId, ProductSku sku) {
        sku.setProductId(productId);
        productSkuCommandService.createSku(sku);
        return sku;
    }

    @Override
    public boolean updateSku(Long productId, Long skuId, ProductSku sku) {
        sku.setId(skuId);
        sku.setProductId(productId);
        return productSkuCommandService.updateSku(sku);
    }

    @Override
    public Boolean updateSkuStatus(Long skuId, String status) {
        return productSkuCommandService.updateStatus(skuId, status);
    }

    @Override
    public Boolean setDefaultSku(Long productId, Long skuId) {
        return productSkuCommandService.setDefault(productId, skuId);
    }

    @Override
    public boolean deleteSku(Long skuId) {
        return productSkuCommandService.deleteSku(skuId);
    }
}
