package com.rymcu.mortise.product.api.facade.impl;

import com.rymcu.mortise.product.api.dto.ApiProductModels.ProductDetailVO;
import com.rymcu.mortise.product.api.dto.ApiProductModels.SkuTargetVO;
import com.rymcu.mortise.product.api.facade.ProductCatalogApiFacade;
import com.rymcu.mortise.product.entity.Product;
import com.rymcu.mortise.product.entity.ProductSku;
import com.rymcu.mortise.product.service.query.ProductQueryService;
import com.rymcu.mortise.product.service.query.ProductSkuQueryService;
import com.rymcu.mortise.product.service.query.ProductSkuTargetQueryService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ProductCatalogApiFacadeImpl implements ProductCatalogApiFacade {

    private static final String ACTIVE_SKU_STATUS = "active";
    private static final Integer ENABLED_TARGET_STATUS = 1;

    private final ProductQueryService productQueryService;
    private final ProductSkuQueryService productSkuQueryService;
    private final ProductSkuTargetQueryService productSkuTargetQueryService;

    public ProductCatalogApiFacadeImpl(
            ProductQueryService productQueryService,
            ProductSkuQueryService productSkuQueryService,
            ProductSkuTargetQueryService productSkuTargetQueryService
    ) {
        this.productQueryService = productQueryService;
        this.productSkuQueryService = productSkuQueryService;
        this.productSkuTargetQueryService = productSkuTargetQueryService;
    }

    @Override
    public List<Product> listProducts(String productType) {
        return productQueryService.findByProductType(productType);
    }

    @Override
    public ProductDetailVO getProductDetail(Long id) {
        Product product = productQueryService.findPublishedById(id);
        if (product == null) {
            return null;
        }

        ProductDetailVO detailVO = new ProductDetailVO();
        BeanUtils.copyProperties(product, detailVO);

        List<ProductSku> skuList = productSkuQueryService.findByProductId(id);
        if (skuList.isEmpty()) {
            detailVO.setSkuTargets(Collections.emptyList());
            return detailVO;
        }

        List<Long> skuIds = skuList.stream()
                .filter(sku -> ACTIVE_SKU_STATUS.equals(sku.getStatus()))
                .map(ProductSku::getId)
                .toList();
        if (skuIds.isEmpty()) {
            detailVO.setSkuTargets(Collections.emptyList());
            return detailVO;
        }

        detailVO.setSkuTargets(productSkuTargetQueryService.findByProductSkuIds(skuIds).stream()
                .filter(target -> ENABLED_TARGET_STATUS.equals(target.getStatus()))
                .map(target -> {
                    SkuTargetVO targetVO = new SkuTargetVO();
                    BeanUtils.copyProperties(target, targetVO);
                    return targetVO;
                })
                .collect(Collectors.toList()));
        return detailVO;
    }

    @Override
    public Map<String, String> listProductTypes() {
        return productQueryService.getAllProductTypes();
    }
}
