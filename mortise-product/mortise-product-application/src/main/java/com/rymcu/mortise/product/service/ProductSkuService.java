package com.rymcu.mortise.product.service;

import com.mybatisflex.core.service.IService;
import com.rymcu.mortise.product.entity.ProductSku;

import java.util.List;

/**
 * 产品 SKU 服务
 *
 * @author ronger
 */
public interface ProductSkuService extends IService<ProductSku> {

    /**
     * 查询指定产品的所有 SKU
     *
     * @param productId 产品ID
     * @return SKU 列表（按 sortNo / createdTime 排序）
     */
    List<ProductSku> findByProductId(Long productId);

    /**
     * 更新 SKU 状态
     *
     * @param id     SKU ID
     * @param status 状态：active / inactive / discontinued
     * @return 是否成功
     */
    Boolean updateStatus(Long id, String status);

    /**
     * 设置默认 SKU
     * <p>将指定 SKU 设为默认，同时将同产品下其他 SKU 的 isDefault 置为 false。
     *
     * @param productId 产品ID
     * @param skuId     目标 SKU ID
     * @return 是否成功
     */
    Boolean setDefault(Long productId, Long skuId);
}
