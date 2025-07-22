package com.rymcu.mortise.core.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应码枚举，作为所有API响应状态的唯一真理来源
 *
 * @author ronger
 */
@Getter
@AllArgsConstructor
public enum ResultCode {
    /**
     * 成功状态
     * 对于特定的成功操作，建议在业务层直接返回带有具体消息的成功结果，
     * 例如 GlobalResult.success("邮件发送成功")
     */
    SUCCESS(200, "操作成功"),

    /**
     * 失败状态
     * 泛用失败，具体错误信息由业务层在调用时传入
     */
    FAIL(400, "操作失败"),

    /**
     * 系统级错误
     */
    UNAUTHENTICATED(401, "认证失败，请重新登录"),
    UNAUTHORIZED(403, "无权访问"),
    NOT_FOUND(404, "请求的资源不存在"),
    INTERNAL_SERVER_ERROR(500, "系统繁忙，请稍后再试"),

    /**
     * 业务级错误（10000开始）
     */
    INVALID_PARAM(10000, "参数错误"),
    UNKNOWN_ACCOUNT(10001, "未知账号"),
    INCORRECT_ACCOUNT_OR_PASSWORD(10002, "账号或密码错误"),
    INVALID_VERIFICATION_CODE(10003, "验证码错误或已失效"), // 原VALIDATE语义不清晰，改为更具体的名称
    SEND_EMAIL_FAIL(10004, "邮件发送失败，请稍后再试"), // 从 GlobalResultMessage 合并而来
    EMAIL_EXISTS(10005, "该邮箱已被注册！"), // 从 GlobalResultMessage 合并而来
    REGISTER_FAIL(10006, "注册失败！"), // 从 GlobalResultMessage 合并而来
    UPDATE_PASSWORD_FAIL(10007, "修改密码失败！");

    private final int code;
    private final String message;
}
