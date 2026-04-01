package com.rymcu.mortise.product.api.facade.impl;

import com.rymcu.mortise.product.api.facade.ProductCatalogApiFacade;
import com.rymcu.mortise.product.entity.Product;
import com.rymcu.mortise.product.service.query.ProductQueryService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ProductCatalogApiFacadeImpl implements ProductCatalogApiFacade {

    private final ProductQueryService productQueryService;

    public ProductCatalogApiFacadeImpl(ProductQueryService productQueryService) {
        this.productQueryService = productQueryService;
    }

    @Override
    public List<Product> listProducts(String productType) {
        return productQueryService.findByProductType(productType);
    }

    @Override
    public Product getProductDetail(Long id) {
        return productQueryService.findPublishedById(id);
    }

    @Override
    public Map<String, String> listProductTypes() {
        return productQueryService.getAllProductTypes();
    }
}
