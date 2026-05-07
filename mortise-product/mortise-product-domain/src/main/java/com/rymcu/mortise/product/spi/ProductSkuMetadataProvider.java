package com.rymcu.mortise.product.spi;

import java.util.List;
import java.util.Map;

/**
 * 产品 SKU 扩展元数据提供者。
 * <p>
 * 业务模块可通过 SPI 为公开商品详情补充定价、库存等领域属性，避免产品模块反向依赖业务模块。
 */
public interface ProductSkuMetadataProvider {

    Map<Long, Map<String, Object>> getSkuMetadata(List<Long> productSkuIds);

    default int getOrder() {
        return 100;
    }
}
