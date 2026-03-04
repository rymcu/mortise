package com.rymcu.mortise.product.enums;

import lombok.Getter;

/**
 * 内置产品类型枚举
 * <p>
 * 仅定义通用的基础类型。各业务域可通过 {@link com.rymcu.mortise.product.spi.ProductTypeProvider}
 * SPI 接口注册自定义类型（如 firmware、device_model 等），无需修改此枚举。
 * <p>
 * {@link com.rymcu.mortise.product.entity.Product#productType} 字段存储字符串 code，
 * 不强制绑定此枚举，因此自定义类型同样可用。
 *
 * @author ronger
 */
@Getter
public enum ProductType {

    /** 标准实物/数字产品 */
    STANDARD("standard", "标准产品"),
    /** 纯数字商品（软件、资料等） */
    DIGITAL("digital", "数字商品"),
    /** 服务类产品 */
    SERVICE("service", "服务"),
    /** 组合套餐 */
    BUNDLE("bundle", "套餐");

    private final String code;
    private final String description;

    ProductType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据 code 查找内置类型，找不到返回 null（可能是 SPI 注册的自定义类型）
     */
    public static ProductType fromCode(String code) {
        for (ProductType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
