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
 * 退款申请实体
 *
 * @author ronger
 */
@Data
@Table(value = "mortise_refund_request", schema = "mortise")
public class RefundRequest implements Serializable {

    /**
     * 主键 ID
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    /**
     * 退款单号
     */
    @NotBlank(message = "退款单号不能为空")
    private String refundNo;

    /**
     * 订单 ID
     */
    @NotNull(message = "订单ID 不能为空")
    private Long orderId;

    /**
     * 订单项ID，NULL表示整单退款
     */
    private Long orderItemId;

    /**
     * 支付交易 ID
     */
    private Long paymentTransactionId;

    /**
     * 会员 ID
     */
    @NotNull(message = "会员 ID 不能为空")
    private Long memberId;

    /**
     * 退款类型：full_order-整单退款, partial_order-部分退款, single_item-单品退款
     */
    @NotBlank(message = "退款类型不能为空")
    private String refundType;

    /**
     * 申请退款金额
     */
    @NotNull(message = "申请退款金额不能为空")
    private BigDecimal requestedAmount;

    /**
     * 批准退款金额
     */
    private BigDecimal approvedAmount;

    /**
     * 实际退款金额
     */
    private BigDecimal actualAmount;

    /**
     * 货币类型
     */
    private String currency;

    /**
     * 退款原因：buyer_request-买家申请, seller_cancel-卖家取消, quality_issue-质量问题, logistics_issue-物流问题, duplicate_payment-重复支付, other-其他
     */
    @NotBlank(message = "退款原因不能为空")
    private String reason;

    /**
     * 详细原因
     */
    private String reasonDetail;

    /**
     * 凭证图片 URL 数组
     */
    private String[] evidenceUrls;

    /**
     * 状态：0-待审核, 1-已批准, 2-已拒绝, 3-处理中, 4-已完成, 5-失败
     */
    private Integer status;

    /**
     * 审核人 ID
     */
    private Long reviewedBy;

    /**
     * 审核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewedTime;

    /**
     * 审核备注
     */
    private String reviewNotes;

    /**
     * 退款方式
     */
    private String refundMethod;

    /**
     * 退款账户
     */
    private String refundAccount;

    /**
     * 处理人 ID
     */
    private Long processedBy;

    /**
     * 处理时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime processedTime;

    /**
     * 完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedTime;

    /**
     * 支付网关退款号
     */
    private String gatewayRefundNo;

    /**
     * 网关响应
     */
    @Column(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> gatewayResponse;

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
}
