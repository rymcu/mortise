package com.rymcu.mortise.product.api.facade.impl;

import com.rymcu.mortise.common.enumerate.Status;
import com.rymcu.mortise.product.api.dto.ApiProductModels.ProductDetailVO;
import com.rymcu.mortise.product.api.dto.ApiProductModels.SkuTargetVO;
import com.rymcu.mortise.product.api.facade.ProductCatalogApiFacade;
import com.rymcu.mortise.product.entity.Product;
import com.rymcu.mortise.product.entity.ProductSku;
import com.rymcu.mortise.product.service.query.ProductQueryService;
import com.rymcu.mortise.product.service.query.ProductSkuQueryService;
import com.rymcu.mortise.product.service.query.ProductSkuTargetQueryService;
import com.rymcu.mortise.product.spi.ProductSkuMetadataProvider;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ProductCatalogApiFacadeImpl implements ProductCatalogApiFacade {

    private static final String ACTIVE_SKU_STATUS = "active";
    private static final Integer ENABLED_TARGET_STATUS = Status.ENABLED.getCode();

    private final ProductQueryService productQueryService;
    private final ProductSkuQueryService productSkuQueryService;
    private final ProductSkuTargetQueryService productSkuTargetQueryService;
    private final List<ProductSkuMetadataProvider> skuMetadataProviders;

    public ProductCatalogApiFacadeImpl(
            ProductQueryService productQueryService,
            ProductSkuQueryService productSkuQueryService,
            ProductSkuTargetQueryService productSkuTargetQueryService,
            List<ProductSkuMetadataProvider> skuMetadataProviders
    ) {
        this.productQueryService = productQueryService;
        this.productSkuQueryService = productSkuQueryService;
        this.productSkuTargetQueryService = productSkuTargetQueryService;
        this.skuMetadataProviders = skuMetadataProviders;
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

        Map<Long, Map<String, Object>> skuMetadata = collectSkuMetadata(skuIds);
        detailVO.setSkuTargets(productSkuTargetQueryService.findByProductSkuIds(skuIds).stream()
                .filter(target -> ENABLED_TARGET_STATUS.equals(target.getStatus()))
                .map(target -> {
                    SkuTargetVO targetVO = new SkuTargetVO();
                    BeanUtils.copyProperties(target, targetVO);
                    targetVO.setMetadata(skuMetadata.getOrDefault(target.getProductSkuId(), Collections.emptyMap()));
                    return targetVO;
                })
                .collect(Collectors.toList()));
        return detailVO;
    }

    @Override
    public Map<String, String> listProductTypes() {
        return productQueryService.getAllProductTypes();
    }

    private Map<Long, Map<String, Object>> collectSkuMetadata(List<Long> skuIds) {
        if (skuIds.isEmpty() || skuMetadataProviders.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, Map<String, Object>> result = new LinkedHashMap<>();
        skuMetadataProviders.stream()
                .sorted(Comparator.comparingInt(ProductSkuMetadataProvider::getOrder))
                .forEach(provider -> provider.getSkuMetadata(skuIds).forEach((skuId, metadata) -> {
                    Map<String, Object> merged = new LinkedHashMap<>(result.getOrDefault(skuId, Collections.emptyMap()));
                    merged.putAll(metadata);
                    result.put(skuId, merged);
                }));
        return result;
    }
}
