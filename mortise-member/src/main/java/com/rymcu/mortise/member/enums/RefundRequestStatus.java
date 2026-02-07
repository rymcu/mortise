package com.rymcu.mortise.member.enums;

import lombok.Getter;

/**
 * Created on 2026/1/13 15:05.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.member.enums
 */
@Getter
public enum RefundRequestStatus {

    /**
     * 待审核
     */
    PENDING(0, "待审批"),
    PASS(1, "已同意"),
    REJECTED(2, "已拒绝"),
    PROCESSING(3, "处理中"),
    COMPLETED(4, "已退款"),
    FAILED(5, "失败"),
    CANCELED(6, "已取消");

    private final int code;

    private final String message;

    RefundRequestStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 根据 code 获取对应的 message
     *
     * @param code 状态码
     * @return 状态描述，如果 code 不存在则返回 null
     */
    public static String getMessageByCode(int code) {
        for (RefundRequestStatus status : RefundRequestStatus.values()) {
            if (status.code == code) {
                return status.message;
            }
        }
        return null;
    }

    /**
     * 根据 code 获取对应的枚举实例
     *
     * @param code 状态码
     * @return 枚举实例，如果 code 不存在则返回 null
     */
    public static RefundRequestStatus fromCode(int code) {
        for (RefundRequestStatus status : RefundRequestStatus.values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }

}
