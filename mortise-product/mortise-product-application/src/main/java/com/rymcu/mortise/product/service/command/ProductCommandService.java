package com.rymcu.mortise.product.service.command;

import com.rymcu.mortise.product.entity.Product;

import java.util.List;

public interface ProductCommandService {

    boolean createProduct(Product product);

    boolean updateProduct(Product product);

    Boolean updateStatus(Long id, Integer status);

    int batchUpdateStatus(List<Long> ids, Integer status);

    boolean deleteProduct(Long id);
}
