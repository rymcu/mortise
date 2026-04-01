package com.rymcu.mortise.product.api.facade;

import com.rymcu.mortise.product.entity.Product;

import java.util.List;
import java.util.Map;

public interface ProductCatalogApiFacade {

    List<Product> listProducts(String productType);

    Product getProductDetail(Long id);

    Map<String, String> listProductTypes();
}
