package com.rymcu.mortise.product.admin.facade;

import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.product.entity.Product;

import java.util.List;
import java.util.Map;

public interface ProductAdminFacade {

    PageResult<Product> listProducts(Integer pageNum, Integer pageSize, String keyword, String productType,
                                     Long categoryId, Integer status, Boolean isFeatured);

    Product getProduct(Long id);

    boolean createProduct(Product product);

    boolean updateProduct(Long id, Product product);

    Boolean updateStatus(Long id, Integer status);

    int batchUpdateStatus(List<Long> ids, Integer status);

    boolean deleteProduct(Long id);

    Map<String, String> listProductTypes();
}
