package com.rymcu.mortise.member.model;

import com.rymcu.mortise.common.model.BaseSearch;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 退款查询条件（客户端）
 *
 * @author ronger
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RefundSearch extends BaseSearch {
    /**
     * 会员ID（由系统自动设置，无需客户端传入）
     */
    private Long memberId;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 退款单号
     */
    private String refundNo;
}
