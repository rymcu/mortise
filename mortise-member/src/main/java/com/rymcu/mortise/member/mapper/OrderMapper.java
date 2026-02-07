package com.rymcu.mortise.member.mapper;

import com.mybatisflex.core.BaseMapper;
import com.rymcu.mortise.member.entity.Order;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单 Mapper
 *
 * @author ronger
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
