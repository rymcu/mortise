package com.rymcu.mortise.product.service.query.impl;

import com.mybatisflex.core.paginate.Page;
import com.rymcu.mortise.product.dto.ProductQueryParam;
import com.rymcu.mortise.product.entity.Product;
import com.rymcu.mortise.product.service.ProductService;
import com.rymcu.mortise.product.service.query.ProductQueryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ProductQueryServiceImpl implements ProductQueryService {

    private final ProductService productService;

    public ProductQueryServiceImpl(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public Page<Product> pageByParam(Page<Product> page, ProductQueryParam param) {
        return productService.pageByParam(page, param);
    }

    @Override
    public Product getById(Long id) {
        return productService.getById(id);
    }

    @Override
    public List<Product> findByProductType(String productType) {
        return productService.findByProductType(productType);
    }

    @Override
    public Product findPublishedById(Long id) {
        return productService.findPublishedById(id);
    }

    @Override
    public Map<String, String> getAllProductTypes() {
        return productService.getAllProductTypes();
    }
}
