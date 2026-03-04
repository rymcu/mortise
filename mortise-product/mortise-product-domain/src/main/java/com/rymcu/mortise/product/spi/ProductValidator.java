package com.rymcu.mortise.product.spi;

import com.rymcu.mortise.product.entity.Product;

import java.util.List;

/**
 * 产品校验扩展 SPI
 * <p>
 * 各业务域可实现此接口注入额外的校验规则。
 * 例如：电商模块要求产品上架前必须关联 SKU 定价信息。
 * <p>
 * 使用示例：
 * <pre>
 * &#64;Component
 * public class CommerceProductValidator implements ProductValidator {
 *     &#64;Override
 *     public List&lt;String&gt; validate(Product product) {
 *         if ("course".equals(product.getProductType()) &amp;&amp; needsPricing(product)) {
 *             return List.of("课程类产品上架前必须设置定价");
 *         }
 *         return List.of();
 *     }
 * }
 * </pre>
 *
 * @author ronger
 */
public interface ProductValidator {

    /**
     * 校验产品数据，返回错误消息列表；空列表表示通过
     *
     * @param product 待校验的产品实体
     * @return 错误消息列表，空集合表示校验通过
     */
    List<String> validate(Product product);

    /**
     * 优先级，数字越小越先执行
     */
    default int getOrder() {
        return 100;
    }
}
