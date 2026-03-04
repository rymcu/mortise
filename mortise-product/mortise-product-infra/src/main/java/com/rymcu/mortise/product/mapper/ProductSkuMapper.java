package com.rymcu.mortise.product.mapper;

import com.mybatisflex.core.BaseMapper;
import com.rymcu.mortise.product.entity.ProductSku;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品SKU Mapper
 *
 * @author ronger
 */
@Mapper
public interface ProductSkuMapper extends BaseMapper<ProductSku> {
}
