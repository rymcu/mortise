package com.rymcu.mortise.product.service.support;

import com.github.f4b6a3.ulid.UlidCreator;

/**
 * 产品编码生成器。
 * <p>
 * 产品编码格式: {@code PRD-{ULID}} — 30 字符，时间有序 + 全局唯一，无枚举风险。
 * <br>
 * 示例: {@code PRD-01J5K3G7P1QX8Y3ZMVKD4WNHT2}
 */
public final class ProductCodeGenerator {

    private static final String PRODUCT_CODE_PREFIX = "PRD-";

    private ProductCodeGenerator() {
    }

    /**
     * 生成产品编码。格式: PRD-{ULID}
     */
    public static String generateProductCode() {
        return PRODUCT_CODE_PREFIX + UlidCreator.getUlid().toString();
    }
}
