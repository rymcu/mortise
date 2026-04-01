package com.rymcu.mortise.product.service.query;

import com.rymcu.mortise.product.entity.ProductCategory;

import java.util.List;

public interface ProductCategoryQueryService {

    List<ProductCategory> getTree();

    List<ProductCategory> getFullTree();

    ProductCategory getById(Long id);
}
