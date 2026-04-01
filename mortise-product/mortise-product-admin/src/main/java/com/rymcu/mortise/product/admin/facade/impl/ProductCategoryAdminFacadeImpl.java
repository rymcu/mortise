package com.rymcu.mortise.product.admin.facade.impl;

import com.rymcu.mortise.product.admin.facade.ProductCategoryAdminFacade;
import com.rymcu.mortise.product.entity.ProductCategory;
import com.rymcu.mortise.product.service.command.ProductCategoryCommandService;
import com.rymcu.mortise.product.service.query.ProductCategoryQueryService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductCategoryAdminFacadeImpl implements ProductCategoryAdminFacade {

    private final ProductCategoryCommandService productCategoryCommandService;
    private final ProductCategoryQueryService productCategoryQueryService;

    public ProductCategoryAdminFacadeImpl(ProductCategoryCommandService productCategoryCommandService,
                                          ProductCategoryQueryService productCategoryQueryService) {
        this.productCategoryCommandService = productCategoryCommandService;
        this.productCategoryQueryService = productCategoryQueryService;
    }

    @Override
    public List<ProductCategory> getFullTree() {
        return productCategoryQueryService.getFullTree();
    }

    @Override
    public ProductCategory getCategory(Long id) {
        return productCategoryQueryService.getById(id);
    }

    @Override
    public boolean createCategory(ProductCategory category) {
        return productCategoryCommandService.createCategory(category);
    }

    @Override
    public boolean updateCategory(Long id, ProductCategory category) {
        category.setId(id);
        return productCategoryCommandService.updateCategory(category);
    }

    @Override
    public Boolean updateStatus(Long id, Integer status) {
        return productCategoryCommandService.updateStatus(id, status);
    }

    @Override
    public boolean deleteCategory(Long id) {
        return productCategoryCommandService.deleteCategory(id);
    }
}
