package com.rymcu.mortise.product.service.query.impl;

import com.rymcu.mortise.product.entity.ProductCategory;
import com.rymcu.mortise.product.service.ProductCategoryService;
import com.rymcu.mortise.product.service.query.ProductCategoryQueryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductCategoryQueryServiceImpl implements ProductCategoryQueryService {

    private final ProductCategoryService productCategoryService;

    public ProductCategoryQueryServiceImpl(ProductCategoryService productCategoryService) {
        this.productCategoryService = productCategoryService;
    }

    @Override
    public List<ProductCategory> getTree() {
        return productCategoryService.getTree();
    }

    @Override
    public List<ProductCategory> getFullTree() {
        return productCategoryService.getFullTree();
    }

    @Override
    public ProductCategory getById(Long id) {
        return productCategoryService.getById(id);
    }
}
