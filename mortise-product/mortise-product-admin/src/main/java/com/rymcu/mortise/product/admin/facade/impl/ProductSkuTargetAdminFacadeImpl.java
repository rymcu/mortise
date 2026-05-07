package com.rymcu.mortise.product.admin.facade.impl;

import com.rymcu.mortise.common.exception.BusinessException;
import com.rymcu.mortise.product.admin.facade.ProductSkuTargetAdminFacade;
import com.rymcu.mortise.product.entity.ProductSku;
import com.rymcu.mortise.product.entity.ProductSkuTarget;
import com.rymcu.mortise.product.service.command.ProductSkuTargetCommandService;
import com.rymcu.mortise.product.service.query.ProductSkuQueryService;
import com.rymcu.mortise.product.service.query.ProductSkuTargetQueryService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductSkuTargetAdminFacadeImpl implements ProductSkuTargetAdminFacade {

    private final ProductSkuQueryService productSkuQueryService;
    private final ProductSkuTargetQueryService productSkuTargetQueryService;
    private final ProductSkuTargetCommandService productSkuTargetCommandService;

    public ProductSkuTargetAdminFacadeImpl(
            ProductSkuQueryService productSkuQueryService,
            ProductSkuTargetQueryService productSkuTargetQueryService,
            ProductSkuTargetCommandService productSkuTargetCommandService
    ) {
        this.productSkuQueryService = productSkuQueryService;
        this.productSkuTargetQueryService = productSkuTargetQueryService;
        this.productSkuTargetCommandService = productSkuTargetCommandService;
    }

    @Override
    public List<ProductSkuTarget> listTargets(Long productId, Long skuId) {
        validateSkuOwnership(productId, skuId);
        return productSkuTargetQueryService.findByProductSkuId(skuId);
    }

    @Override
    public ProductSkuTarget getTarget(Long productId, Long skuId, Long targetId) {
        validateSkuOwnership(productId, skuId);
        ProductSkuTarget target = productSkuTargetQueryService.getById(targetId);
        if (target == null || !skuId.equals(target.getProductSkuId())) {
            throw new BusinessException("SKU 目标映射不存在");
        }
        return target;
    }

    @Override
    public ProductSkuTarget createTarget(Long productId, Long skuId, ProductSkuTarget target) {
        validateSkuOwnership(productId, skuId);
        target.setProductSkuId(skuId);
        productSkuTargetCommandService.createTarget(target);
        return target;
    }

    @Override
    public boolean updateTarget(Long productId, Long skuId, Long targetId, ProductSkuTarget target) {
        getTarget(productId, skuId, targetId);
        target.setId(targetId);
        target.setProductSkuId(skuId);
        return productSkuTargetCommandService.updateTarget(target);
    }

    @Override
    public boolean deleteTarget(Long productId, Long skuId, Long targetId) {
        getTarget(productId, skuId, targetId);
        return productSkuTargetCommandService.deleteTarget(targetId);
    }

    private void validateSkuOwnership(Long productId, Long skuId) {
        ProductSku sku = productSkuQueryService.getById(skuId);
        if (sku == null || !productId.equals(sku.getProductId())) {
            throw new BusinessException("SKU 不存在或不属于当前产品");
        }
    }
}
