package com.rymcu.mortise.member.mapper;

import com.mybatisflex.core.BaseMapper;
import com.rymcu.mortise.member.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单项 Mapper
 *
 * @author ronger
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
}
