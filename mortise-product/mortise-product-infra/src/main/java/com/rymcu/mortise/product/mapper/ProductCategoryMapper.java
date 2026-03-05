package com.rymcu.mortise.product.mapper;

import com.mybatisflex.core.BaseMapper;
import com.rymcu.mortise.product.entity.ProductCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 产品分类 Mapper
 *
 * @author ronger
 */
@Mapper
public interface ProductCategoryMapper extends BaseMapper<ProductCategory> {
}
