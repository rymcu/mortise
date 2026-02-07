package com.rymcu.mortise.member.model;

import com.rymcu.mortise.common.model.BaseSearch;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 订单查询参数
 *
 * @author ronger
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderSearch extends BaseSearch {

    /**
     * 会员ID
     */
    private Long memberId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 订单类型
     */
    private String orderType;
}
