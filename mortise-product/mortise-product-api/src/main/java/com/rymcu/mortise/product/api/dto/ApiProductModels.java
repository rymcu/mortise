package com.rymcu.mortise.product.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class ApiProductModels {

    private ApiProductModels() {
    }

    @Data
    public static class ProductDetailVO {

        private Long id;
        private String productCode;
        private String title;
        private String subtitle;
        private String description;
        private String shortDescription;
        private String coverImageUrl;
        private List<String> galleryImages;
        private String productType;
        private Long categoryId;
        private String[] tags;
        private Map<String, Object> features;
        private Map<String, Object> specifications;
        private String seoTitle;
        private String seoDescription;
        private String seoKeywords;
        private Integer status;
        private Boolean isFeatured;
        private Integer sortNo;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdTime;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updatedTime;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime publishedTime;
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
