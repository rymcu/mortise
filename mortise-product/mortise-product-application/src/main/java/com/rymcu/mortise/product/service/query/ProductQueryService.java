package com.rymcu.mortise.product.service.query;

import com.mybatisflex.core.paginate.Page;
import com.rymcu.mortise.product.dto.ProductQueryParam;
import com.rymcu.mortise.product.entity.Product;

import java.util.List;
import java.util.Map;

public interface ProductQueryService {

    Page<Product> pageByParam(Page<Product> page, ProductQueryParam param);

    Product getById(Long id);

    List<Product> findByProductType(String productType);

    Product findPublishedById(Long id);

    Map<String, String> getAllProductTypes();
}
