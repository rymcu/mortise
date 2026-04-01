package com.rymcu.mortise.product.service.command;

import com.rymcu.mortise.product.entity.ProductCategory;

public interface ProductCategoryCommandService {

    boolean createCategory(ProductCategory category);

    boolean updateCategory(ProductCategory category);

    Boolean updateStatus(Long id, Integer status);

    boolean deleteCategory(Long id);
}
