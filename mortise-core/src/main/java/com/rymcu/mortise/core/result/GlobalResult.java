package com.rymcu.mortise.core.result;

import lombok.Getter;
import lombok.ToString;

/**
 * 全局统一返回结果类
 *
 * @author ronger
 */
@Getter
@ToString
public class GlobalResult<T> {

    private final int code;
    private final String message;
    private final T data;

    /**
     * 将构造函数私有化，强制使用静态工厂方法创建实例
     */
    private GlobalResult(ResultCode resultCode, T data) {
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
        this.data = data;
    }

    private GlobalResult(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // --- 成功相关的静态工厂方法 ---

    /**
     * 成功，不返回数据
     */
    public static <T> GlobalResult<T> success() {
        return new GlobalResult<>(ResultCode.SUCCESS, null);
    }

    /**
     * 成功，返回数据
     */
    public static <T> GlobalResult<T> success(T data) {
        return new GlobalResult<>(ResultCode.SUCCESS, data);
    }

    /**
     * 成功，返回自定义消息和数据
     */
    public static <T> GlobalResult<T> success(String message, T data) {
        return new GlobalResult<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    // --- 失败相关的静态工厂方法 ---

    /**
     * 失败，使用默认的 FAIL 状态码和消息
     */
    public static <T> GlobalResult<T> error() {
        return new GlobalResult<>(ResultCode.BAD_REQUEST, null);
    }

    /**
     * 失败，只返回自定义消息
     */
    public static <T> GlobalResult<T> error(String message) {
        return new GlobalResult<>(ResultCode.BAD_REQUEST.getCode(), message, null);
    }

    /**
     * 失败，返回自定义状态码和消息
     */
    public static <T> GlobalResult<T> error(int code, String message) {
        return new GlobalResult<>(code, message, null);
    }

    /**
     * 失败，使用指定的 ResultCode
     */
    public static <T> GlobalResult<T> error(ResultCode resultCode) {
        return new GlobalResult<>(resultCode, null);
    }

    /**
     * 失败，使用指定的 ResultCode 和自定义消息
     */
    public static <T> GlobalResult<T> error(ResultCode resultCode, String message) {
        return new GlobalResult<>(resultCode.getCode(), message, null);
    }
}
