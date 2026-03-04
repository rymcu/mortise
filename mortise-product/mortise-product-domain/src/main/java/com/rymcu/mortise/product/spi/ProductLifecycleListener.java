package com.rymcu.mortise.product.spi;

import com.rymcu.mortise.product.entity.Product;

/**
 * 产品生命周期事件监听 SPI
 * <p>
 * 各业务域可实现此接口监听产品状态变更事件，执行各自的联动逻辑。
 * 例如：电商模块在产品下架时同步下架所有 SKU 定价。
 * <p>
 * 使用示例：
 * <pre>
 * &#64;Component
 * public class CommerceProductLifecycleListener implements ProductLifecycleListener {
 *     &#64;Override
 *     public void onStatusChanged(Product product, int oldStatus, int newStatus) {
 *         if (newStatus == 2) { // 下架
 *             skuPricingService.deactivateByProductId(product.getId());
 *         }
 *     }
 * }
 * </pre>
 *
 * @author ronger
 */
public interface ProductLifecycleListener {

    /**
     * 产品创建后回调
     *
     * @param product 已创建的产品
     */
    default void onCreated(Product product) {
    }

    /**
     * 产品状态变更后回调
     *
     * @param product   产品实体
     * @param oldStatus 旧状态
     * @param newStatus 新状态
     */
    default void onStatusChanged(Product product, int oldStatus, int newStatus) {
    }

    /**
     * 产品逻辑删除后回调
     *
     * @param product 被删除的产品
     */
    default void onDeleted(Product product) {
    }

    /**
     * 优先级，数字越小越先执行
     */
    default int getOrder() {
        return 100;
    }
}
