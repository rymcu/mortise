package com.rymcu.mortise.product.admin.facade.impl;

import com.mybatisflex.core.paginate.Page;
import com.rymcu.mortise.product.admin.facade.ProductAdminFacade;
import com.rymcu.mortise.product.dto.ProductQueryParam;
import com.rymcu.mortise.product.entity.Product;
import com.rymcu.mortise.product.service.command.ProductCommandService;
import com.rymcu.mortise.product.service.query.ProductQueryService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ProductAdminFacadeImpl implements ProductAdminFacade {

    private final ProductCommandService productCommandService;
    private final ProductQueryService productQueryService;

    public ProductAdminFacadeImpl(ProductCommandService productCommandService,
                                  ProductQueryService productQueryService) {
        this.productCommandService = productCommandService;
        this.productQueryService = productQueryService;
    }

    @Override
    public Page<Product> listProducts(Integer pageNum, Integer pageSize, String keyword, String productType,
                                      Long categoryId, Integer status, Boolean isFeatured) {
        Page<Product> page = new Page<>(pageNum, pageSize);
        ProductQueryParam param = new ProductQueryParam(keyword, productType, categoryId, status, isFeatured);
        return productQueryService.pageByParam(page, param);
    }

    @Override
    public Product getProduct(Long id) {
        return productQueryService.getById(id);
    }

    @Override
    public boolean createProduct(Product product) {
        return productCommandService.createProduct(product);
    }

    @Override
    public boolean updateProduct(Long id, Product product) {
        product.setId(id);
        return productCommandService.updateProduct(product);
    }

    @Override
    public Boolean updateStatus(Long id, Integer status) {
        return productCommandService.updateStatus(id, status);
    }

    @Override
    public int batchUpdateStatus(List<Long> ids, Integer status) {
        return productCommandService.batchUpdateStatus(ids, status);
    }

    @Override
    public boolean deleteProduct(Long id) {
        return productCommandService.deleteProduct(id);
    }

    @Override
    public Map<String, String> listProductTypes() {
        return productQueryService.getAllProductTypes();
    }
}
