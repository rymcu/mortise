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
 * 订单商品项实体
 *
 * @author ronger
 */
@Data
@Table(value = "mortise_order_item", schema = "mortise")
public class OrderItem implements Serializable {

    /**
     * 主键ID
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    /**
     * 订单ID
     */
    @NotNull(message = "订单ID 不能为空")
    private Long orderId;

    /**
     * 商品ID
     */
    @NotNull(message = "商品ID 不能为空")
    private Long productId;

    /**
     * 商品SKU ID
     */
    @NotNull(message = "商品SKU ID 不能为空")
    private Long productSkuId;

    /**
     * 商品标题快照
     */
    @NotBlank(message = "商品标题不能为空")
    private String productTitle;

    /**
     * SKU名称快照
     */
    @NotBlank(message = "SKU名称不能为空")
    private String skuName;

    /**
     * SKU编码快照
     */
    @NotBlank(message = "SKU编码不能为空")
    private String skuCode;

    /**
     * SKU属性快照
     */
    @Column(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> skuAttributes;

    /**
     * 单价
     */
    @NotNull(message = "单价不能为空")
    private BigDecimal unitPrice;

    /**
     * 数量
     */
    @NotNull(message = "数量不能为空")
    private Integer quantity;

    /**
     * 小计 = unit_price * quantity
     */
    @NotNull(message = "小计不能为空")
    private BigDecimal subtotal;

    /**
     * 单品优惠金额
     */
    private BigDecimal discountAmount;

    /**
     * 最终金额
     */
    @NotNull(message = "最终金额不能为空")
    private BigDecimal finalAmount;

    /**
     * 履约状态：0-待处理, 1-已履约, 2-已取消, 3-已退款
     */
    private Integer fulfillmentStatus;

    /**
     * 履约时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fulfilledTime;

    /**
     * 取消时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelledTime;

    /**
     * 退款状态：0-无, 1-部分退款, 2-全额退款, 3-处理中
     */
    private Integer refundStatus;

    /**
     * 已退款金额
     */
    private BigDecimal refundedAmount;

    /**
     * 退款原因
     */
    private String refundReason;

    /**
     * 扩展元数据
     */
    @Column(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> metadata;

    /**
     * 状态：0-正常, 1-禁用
     */
    private Integer status;

    /**
     * 删除标记：0-未删除, 1-已删除
     */
    @Column(isLogicDelete = true)
    private Integer delFlag;

    /**
     * 创建时间
     */
    @Column(onInsertValue = "now()")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @Column(onInsertValue = "now()", onUpdateValue = "now()")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;
}
