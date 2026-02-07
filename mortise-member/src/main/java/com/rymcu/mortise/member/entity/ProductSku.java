package com.rymcu.mortise.member.entity;

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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 商品SKU实体
 *
 * @author ronger
 */
@Data
@Table(value = "mortise_product_sku", schema = "mortise")
public class ProductSku implements Serializable {

    /**
     * 主键ID
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    /**
     * 商品ID
     */
    @NotNull(message = "商品ID 不能为空")
    private Long productId;

    /**
     * SKU编码
     */
    @NotBlank(message = "SKU编码不能为空")
    private String skuCode;

    /**
     * SKU名称
     */
    @NotBlank(message = "SKU名称不能为空")
    private String name;

    /**
     * SKU描述
     */
    private String description;

    /**
     * SKU属性(如规格、颜色等)
     */
    @Column(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> attributes;

    /**
     * 原价
     */
    @NotNull(message = "原价不能为空")
    private BigDecimal originalPrice;

    /**
     * 当前价格
     */
    @NotNull(message = "当前价格不能为空")
    private BigDecimal currentPrice;

    /**
     * 成本价
     */
    private BigDecimal costPrice;

    /**
     * 货币类型
     */
    @NotBlank(message = "货币类型不能为空")
    private String currency;

    /**
     * 库存类型：unlimited-无限, limited-有限, preorder-预售
     */
    @NotBlank(message = "库存类型不能为空")
    private String inventoryType;

    /**
     * 库存数量
     */
    private Integer stockQuantity;

    /**
     * 预留库存
     */
    private Integer reservedQuantity;

    /**
     * 低库存阈值
     */
    private Integer lowStockThreshold;

    /**
     * 销售开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sellStartTime;

    /**
     * 销售结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sellEndTime;

    /**
     * 最大购买数量
     */
    private Integer maxPurchaseQuantity;

    /**
     * 最小购买数量
     */
    private Integer minPurchaseQuantity;

    /**
     * 状态：active-上架, inactive-下架, out_of_stock-缺货, discontinued-停产
     */
    @NotBlank(message = "状态不能为空")
    private String status;

    /**
     * 是否默认SKU
     */
    private Boolean isDefault;

    /**
     * 重量(克)
     */
    private Integer weightGrams;

    /**
     * 尺寸信息
     */
    @Column(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> dimensions;

    /**
     * 物流模板ID
     */
    private Long shippingTemplateId;

    /**
     * 销售数量
     */
    private Integer saleCount;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 扩展元数据
     */
    @Column(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> metadata;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;

    /**
     * 删除标记：0-未删除, 1-已删除
     */
    @Column(isLogicDelete = true)
    private Integer delFlag;
}
