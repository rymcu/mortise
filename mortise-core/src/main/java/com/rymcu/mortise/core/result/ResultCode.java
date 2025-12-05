package com.rymcu.mortise.core.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应码枚举
 *
 * @author ronger
 */
@Getter
@AllArgsConstructor
public enum ResultCode {
    /**
     * 成功状态
     */
    SUCCESS(200, "操作成功"),

    /**
     * 失败状态
     */
    BAD_REQUEST(400, "操作失败"),

    /**
     * 系统级错误
     */
    UNAUTHENTICATED(401, "认证失败，请重新登录"),
    UNAUTHORIZED(403, "无权访问"),
    NOT_FOUND(404, "请求的资源不存在"),
    DATA_CONFLICT(409, "数据冲突"),
    INTERNAL_SERVER_ERROR(500, "系统繁忙，请稍后再试"),
    // 为兼容旧代码添加别名
    SERVER_ERROR(500, "系统繁忙，请稍后再试"),

    /**
     * 业务级错误（10000开始）
     */
    INVALID_PARAM(10000, "参数错误"),
    UNKNOWN_ACCOUNT(10001, "未知账号"),
    INCORRECT_ACCOUNT_OR_PASSWORD(10002, "账号或密码错误"),
    INVALID_VERIFICATION_CODE(10003, "验证码错误或已失效"),
    SEND_EMAIL_FAIL(10004, "邮件发送失败，请稍后再试"),
    EMAIL_EXISTS(10005, "该邮箱已被注册！"),
    REGISTER_FAIL(10006, "注册失败！"),
    UPDATE_PASSWORD_FAIL(10007, "修改密码失败！");

    private final int code;
    private final String message;
}
