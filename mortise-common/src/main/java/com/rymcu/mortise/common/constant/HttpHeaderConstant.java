package com.rymcu.mortise.common.constant;

/**
 * HTTP 请求头相关常量
 *
 * @author ronger
 */
public final class HttpHeaderConstant {

    /**
     * nginx/网关可传递的客户端类型请求头
     */
    public static final String X_CLIENT_TYPE = "X-Client-Type";

    /**
     * nginx 传递的原始请求 URI（代理前）
     */
    public static final String X_ORIGINAL_URI = "X-Original-URI";

    private HttpHeaderConstant() {
        throw new AssertionError("常量类不应该被实例化");
    }
}
