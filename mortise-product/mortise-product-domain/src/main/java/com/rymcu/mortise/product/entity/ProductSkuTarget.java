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
 * 产品SKU目标关联实体
 *
 * @author ronger
 */
@Data
@Table(value = "mortise_product_sku_target", schema = "mortise")
public class ProductSkuTarget implements Serializable {

    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    @NotNull(message = "产品SKU ID 不能为空")
    private Long productSkuId;

    @NotBlank(message = "目标类型不能为空")
    private String targetType;

    @NotNull(message = "关联目标实体ID 不能为空")
    private Long targetId;

    @NotNull(message = "发放数量不能为空")
    private Integer quantity;

    private Integer validityDays;

    private String accessLevel;

    @Column(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> conditions;

    @Column(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> metadata;

    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;

    @Column(isLogicDelete = true)
    private Integer delFlag;
}
