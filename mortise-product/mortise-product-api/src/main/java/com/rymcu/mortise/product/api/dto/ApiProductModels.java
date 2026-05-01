package com.rymcu.mortise.product.api.dto;

import com.rymcu.mortise.product.entity.Product;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

public final class ApiProductModels {

    private ApiProductModels() {
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class ProductDetailVO extends Product {

        private List<SkuTargetVO> skuTargets;
    }

    @Data
    public static class SkuTargetVO {

        private Long id;
        private Long productSkuId;
        private String targetType;
        private Long targetId;
        private Integer quantity;
        private Integer validityDays;
        private String accessLevel;
    }
}
