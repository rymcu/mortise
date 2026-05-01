package com.rymcu.mortise.product.service.query.impl;

import com.rymcu.mortise.product.entity.ProductSkuTarget;
import com.rymcu.mortise.product.service.ProductSkuTargetService;
import com.rymcu.mortise.product.service.query.ProductSkuTargetQueryService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class ProductSkuTargetQueryServiceImpl implements ProductSkuTargetQueryService {

    private final ProductSkuTargetService productSkuTargetService;

    public ProductSkuTargetQueryServiceImpl(ProductSkuTargetService productSkuTargetService) {
        this.productSkuTargetService = productSkuTargetService;
    }

    @Override
    public List<ProductSkuTarget> findByProductSkuId(Long productSkuId) {
        return productSkuTargetService.findByProductSkuId(productSkuId);
    }

    @Override
    public List<ProductSkuTarget> findByProductSkuIds(Collection<Long> productSkuIds) {
        return productSkuTargetService.findByProductSkuIds(productSkuIds);
    }

    @Override
    public ProductSkuTarget getById(Long id) {
        return productSkuTargetService.getById(id);
    }
}
