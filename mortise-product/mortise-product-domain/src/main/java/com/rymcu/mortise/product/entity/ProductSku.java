package com.rymcu.mortise.product.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import com.rymcu.mortise.persistence.mybatis.handler.JsonbTypeHandler;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 产品SKU实体 — 通用规格单元
 * <p>
 * 仅保留SKU的骨架信息（编码、名称、属性），不包含定价、库存、物流等交易字段。
 * 电商场景的定价/库存由 commerce 模块的 SkuPricing 实体扩展。
 *
 * @author ronger
 */
@Data
@Table(value = "mortise_product_sku", schema = "mortise")
public class ProductSku implements Serializable {

    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    /** 产品ID — 由 URL 路径变量 /products/{productId}/skus 注入，不在请求体中校验 */
    private Long productId;

    /** SKU编码 */
    @NotBlank(message = "SKU编码不能为空")
    private String skuCode;

    /** SKU名称 */
    @NotBlank(message = "SKU名称不能为空")
    private String name;

    /** SKU描述 */
    private String description;

    /** SKU属性（如规格、颜色等） */
    @Column(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> attributes;

    /** 状态：active-上架, inactive-下架, discontinued-停用 */
    @NotBlank(message = "状态不能为空")
    private String status;

    /** 是否默认SKU */
    private Boolean isDefault;

    /** 扩展元数据 */
    @Column(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> metadata;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;

    @Column(isLogicDelete = true)
    private Integer delFlag;
}
