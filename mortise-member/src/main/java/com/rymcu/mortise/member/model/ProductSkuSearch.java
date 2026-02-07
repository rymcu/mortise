package com.rymcu.mortise.member.model;

import com.rymcu.mortise.common.model.BaseSearch;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品SKU搜索条件
 *
 * @author ronger
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductSkuSearch extends BaseSearch {

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 库存类型
     */
    private String inventoryType;

    /**
     * SKU状态：active-上架, inactive-下架, out_of_stock-缺货, discontinued-停产
     */
    private String skuStatus;

    /**
     * 是否默认SKU
     */
    private Boolean isDefault;
}
