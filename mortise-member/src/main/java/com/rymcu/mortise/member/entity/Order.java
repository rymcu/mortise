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
 * 订单实体
 *
 * @author ronger
 */
@Data
@Table(value = "mortise_order", schema = "mortise")
public class Order implements Serializable {

    /**
     * 主键ID
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    /**
     * 订单号
     */
    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    /**
     * 会员ID
     */
    @NotNull(message = "会员ID 不能为空")
    private Long memberId;

    /**
     * 商品小计
     */
    @NotNull(message = "商品小计不能为空")
    private BigDecimal subtotalAmount;

    /**
     * 优惠金额
     */
    private BigDecimal discountAmount;

    /**
     * 运费
     */
    private BigDecimal shippingAmount;

    /**
     * 税费
     */
    private BigDecimal taxAmount;

    /**
     * 订单总额
     */
    @NotNull(message = "订单总额不能为空")
    private BigDecimal totalAmount;

    /**
     * 已支付金额
     */
    private BigDecimal paidAmount;

    /**
     * 货币类型
     */
    private String currency;

    /**
     * 订单状态：0-待支付, 1-已支付, 2-处理中, 3-已发货, 4-已送达, 5-已完成, 6-已取消, 7-已退款, 8-已关闭
     */
    private Integer status;

    /**
     * 支付状态：0-未支付, 1-支付中, 2-已支付, 3-已退款, 4-支付失败
     */
    private Integer paymentStatus;

    /**
     * 履约状态：0-未履约, 1-部分履约, 2-已履约, 3-已交付
     */
    private Integer fulfillmentStatus;

    /**
     * 创建时间
     */
    @Column(onInsertValue = "now()")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    /**
     * 支付时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paidTime;

    /**
     * 发货时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime shippedTime;

    /**
     * 送达时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deliveredTime;

    /**
     * 完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedTime;

    /**
     * 取消时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelledTime;

    /**
     * 订单过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresTime;

    /**
     * 订单来源
     */
    private String source;

    /**
     * 渠道
     */
    private String channel;

    /**
     * 来源URL
     */
    private String referrerUrl;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 买家备注
     */
    private String buyerNotes;

    /**
     * 卖家备注
     */
    private String sellerNotes;

    /**
     * 内部备注
     */
    private String internalNotes;

    /**
     * 扩展元数据
     */
    @Column(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> metadata;

    /**
     * 删除标记：0-未删除, 1-已删除
     */
    @Column(isLogicDelete = true)
    private Integer delFlag;

    /**
     * 更新时间
     */
    @Column(onInsertValue = "now()", onUpdateValue = "now()")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;
}
