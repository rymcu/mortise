package com.rymcu.mortise.product.service.command.impl;

import com.rymcu.mortise.product.entity.ProductCategory;
import com.rymcu.mortise.product.service.ProductCategoryService;
import com.rymcu.mortise.product.service.command.ProductCategoryCommandService;
import org.springframework.stereotype.Service;

@Service
public class ProductCategoryCommandServiceImpl implements ProductCategoryCommandService {

    private final ProductCategoryService productCategoryService;

    public ProductCategoryCommandServiceImpl(ProductCategoryService productCategoryService) {
        this.productCategoryService = productCategoryService;
    }

    @Override
    public boolean createCategory(ProductCategory category) {
        return productCategoryService.save(category);
    }

    @Override
    public boolean updateCategory(ProductCategory category) {
        return productCategoryService.updateById(category);
    }

    @Override
    public Boolean updateStatus(Long id, Integer status) {
        return productCategoryService.updateStatus(id, status);
    }

    @Override
    public boolean deleteCategory(Long id) {
        return productCategoryService.removeById(id);
    }
}
