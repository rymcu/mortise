package com.rymcu.mortise.core.result;

import lombok.Getter;
import lombok.ToString;

/**
 * 全局统一返回结果类
 *
 * @author ronger
 */
@Getter
@ToString // 使用 @ToString 代替 @Data 的部分功能，因为字段设为 final 后不需要 setter
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
     * @return GlobalResult
     */
    public static <T> GlobalResult<T> success() {
        return new GlobalResult<>(ResultCode.SUCCESS, null);
    }

    /**
     * 成功，返回数据
     * @param data 返回的数据
     * @return GlobalResult<T>
     */
    public static <T> GlobalResult<T> success(T data) {
        return new GlobalResult<>(ResultCode.SUCCESS, data);
    }

    /**
     * 成功，返回自定义消息和数据
     * @param message 自定义消息
     * @param data 返回的数据
     * @return GlobalResult<T>
     */
    public static <T> GlobalResult<T> success(String message, T data) {
        return new GlobalResult<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    // --- 失败相关的静态工厂方法 ---

    /**
     * 失败，使用默认的 FAIL 状态码和消息
     * @return GlobalResult
     */
    public static <T> GlobalResult<T> error() {
        return new GlobalResult<>(ResultCode.FAIL, null);
    }

    /**
     * 失败，只返回自定义消息
     * @param message 错误信息
     * @return GlobalResult
     */
    public static <T> GlobalResult<T> error(String message) {
        return new GlobalResult<>(ResultCode.FAIL.getCode(), message, null);
    }

    /**
     * 失败，返回自定义状态码和消息
     * @param message 错误信息
     * @return GlobalResult
     */
    public static <T> GlobalResult<T> error(int code, String message) {
        return new GlobalResult<>(code, message, null);
    }

    /**
     * 失败，使用指定的 ResultCode
     * @param resultCode 错误码枚举
     * @return GlobalResult
     */
    public static <T> GlobalResult<T> error(ResultCode resultCode) {
        return new GlobalResult<>(resultCode, null);
    }

    /**
     * 失败，使用指定的 ResultCode 和自定义消息
     * @param resultCode 错误码枚举
     * @param message 自定义错误信息
     * @return GlobalResult
     */
    public static <T> GlobalResult<T> error(ResultCode resultCode, String message) {
        return new GlobalResult<>(resultCode.getCode(), message, null);
    }

}
