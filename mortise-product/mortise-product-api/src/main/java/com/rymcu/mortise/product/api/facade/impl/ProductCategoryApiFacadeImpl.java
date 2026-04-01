package com.rymcu.mortise.product.api.facade.impl;

import com.rymcu.mortise.product.api.facade.ProductCategoryApiFacade;
import com.rymcu.mortise.product.entity.ProductCategory;
import com.rymcu.mortise.product.service.query.ProductCategoryQueryService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductCategoryApiFacadeImpl implements ProductCategoryApiFacade {

    private final ProductCategoryQueryService productCategoryQueryService;

    public ProductCategoryApiFacadeImpl(ProductCategoryQueryService productCategoryQueryService) {
        this.productCategoryQueryService = productCategoryQueryService;
    }

    @Override
    public List<ProductCategory> getTree() {
        return productCategoryQueryService.getTree();
    }
}
