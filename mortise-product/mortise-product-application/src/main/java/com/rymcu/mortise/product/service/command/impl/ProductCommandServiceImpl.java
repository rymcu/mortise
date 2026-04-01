package com.rymcu.mortise.product.service.command.impl;

import com.rymcu.mortise.product.entity.Product;
import com.rymcu.mortise.product.service.ProductService;
import com.rymcu.mortise.product.service.command.ProductCommandService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductCommandServiceImpl implements ProductCommandService {

    private final ProductService productService;

    public ProductCommandServiceImpl(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public boolean createProduct(Product product) {
        return productService.save(product);
    }

    @Override
    public boolean updateProduct(Product product) {
        return productService.updateById(product);
    }

    @Override
    public Boolean updateStatus(Long id, Integer status) {
        return productService.updateStatus(id, status);
    }

    @Override
    public int batchUpdateStatus(List<Long> ids, Integer status) {
        return productService.batchUpdateStatus(ids, status);
    }

    @Override
    public boolean deleteProduct(Long id) {
        return productService.removeById(id);
    }
}
