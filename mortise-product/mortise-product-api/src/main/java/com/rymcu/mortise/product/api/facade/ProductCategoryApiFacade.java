package com.rymcu.mortise.product.api.facade;

import com.rymcu.mortise.product.entity.ProductCategory;

import java.util.List;

public interface ProductCategoryApiFacade {

    List<ProductCategory> getTree();
}
