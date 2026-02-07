package com.rymcu.mortise.member.mapper;

import com.mybatisflex.core.BaseMapper;
import com.rymcu.mortise.member.entity.Product;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品 Mapper
 *
 * @author ronger
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
