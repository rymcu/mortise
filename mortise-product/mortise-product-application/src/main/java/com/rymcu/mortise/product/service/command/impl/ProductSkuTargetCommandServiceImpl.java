package com.rymcu.mortise.product.service.command.impl;

import com.rymcu.mortise.product.entity.ProductSkuTarget;
import com.rymcu.mortise.product.service.ProductSkuTargetService;
import com.rymcu.mortise.product.service.command.ProductSkuTargetCommandService;
import org.springframework.stereotype.Service;

@Service
public class ProductSkuTargetCommandServiceImpl implements ProductSkuTargetCommandService {

    private final ProductSkuTargetService productSkuTargetService;

    public ProductSkuTargetCommandServiceImpl(ProductSkuTargetService productSkuTargetService) {
        this.productSkuTargetService = productSkuTargetService;
    }

    @Override
    public boolean createTarget(ProductSkuTarget target) {
        return productSkuTargetService.save(target);
    }

    @Override
    public boolean updateTarget(ProductSkuTarget target) {
        return productSkuTargetService.updateById(target);
    }

    @Override
    public boolean deleteTarget(Long targetId) {
        return productSkuTargetService.removeById(targetId);
    }
}
