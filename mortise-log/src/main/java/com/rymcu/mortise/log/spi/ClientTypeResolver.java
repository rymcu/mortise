package com.rymcu.mortise.log.spi;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 客户端类型解析器 SPI 接口
 * <p>
 * 用于根据请求信息确定客户端类型（system/app/web/api 等）
 * 各业务模块可实现此接口定义自己的解析规则
 *
 * @author ronger
 */
public interface ClientTypeResolver {

    /**
     * 客户端类型常量
     */
    String CLIENT_TYPE_SYSTEM = "system";   // 后台管理系统
    String CLIENT_TYPE_APP = "app";         // App端
    String CLIENT_TYPE_WEB = "web";         // Web前端
    String CLIENT_TYPE_API = "api";         // 开放API
    String CLIENT_TYPE_UNKNOWN = "unknown"; // 未知

    /**
     * 获取优先级，数字越小优先级越高
     * 优先级高的解析器会先被调用
     *
     * @return 优先级
     */
    default int getOrder() {
        return 100;
    }

    /**
     * 判断是否支持当前请求
     *
     * @param request HTTP请求
     * @return true: 支持, false: 不支持
     */
    boolean supports(HttpServletRequest request);

    /**
     * 解析客户端类型
     *
     * @param request HTTP请求
     * @return 客户端类型
     */
    String resolve(HttpServletRequest request);
}
