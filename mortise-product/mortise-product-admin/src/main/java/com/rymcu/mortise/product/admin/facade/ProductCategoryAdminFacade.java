package com.rymcu.mortise.product.admin.facade;

import com.rymcu.mortise.product.entity.ProductCategory;

import java.util.List;

public interface ProductCategoryAdminFacade {

    List<ProductCategory> getFullTree();

    ProductCategory getCategory(Long id);

    boolean createCategory(ProductCategory category);

    boolean updateCategory(Long id, ProductCategory category);

    Boolean updateStatus(Long id, Integer status);

    boolean deleteCategory(Long id);
}
