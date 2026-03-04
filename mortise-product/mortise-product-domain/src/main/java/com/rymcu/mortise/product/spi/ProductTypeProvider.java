package com.rymcu.mortise.product.spi;

import java.util.Map;

/**
 * 产品类型扩展 SPI
 * <p>
 * 各业务域实现此接口注册自定义产品类型。
 * 例如：电商模块注册 "course"、"membership"，IoT 模块注册 "firmware"、"device_model"。
 * <p>
 * 使用示例：
 * <pre>
 * &#64;Component
 * public class CommerceProductTypeProvider implements ProductTypeProvider {
 *     &#64;Override
 *     public Map&lt;String, String&gt; getProductTypes() {
 *         return Map.of(
 *             "course", "课程",
 *             "membership", "会员",
 *             "live_event", "直播活动"
 *         );
 *     }
 * }
 * </pre>
 *
 * @author ronger
 */
public interface ProductTypeProvider {

    /**
     * 返回自定义产品类型映射（code → 描述）
     *
     * @return 产品类型 code 到中文描述的映射
     */
    Map<String, String> getProductTypes();

    /**
     * 优先级，数字越小越优先（用于类型冲突时的优先选择）
     */
    default int getOrder() {
        return 100;
    }
}
