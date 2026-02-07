package com.rymcu.mortise.member.service;

import com.mybatisflex.core.service.IService;
import com.rymcu.mortise.member.entity.Order;

/**
 * 订单基础服务
 * <p>
 * 提供订单的基础 CRUD 操作
 * <p>
 * 业务场景说明：
 * - 管理端（mortise-edu）：需要订单列表查询、订单状态变更、订单详情等功能
 * - 客户端（mortise-api）：需要订单创建、支付、取消、确认收货等完整流程
 * <p>
 * 设计原则：
 * - ✅ 本接口仅提供最基础的 CRUD（继承自 IService）
 * - ✅ 特定业务场景的方法应在各业务模块中扩展实现
 * - ❌ 不在此接口中定义特定场景的业务方法
 * <p>
 * 扩展示例：
 * <pre>
 * // mortise-api 中扩展（客户端需要完整订单流程）
 * public interface ApiOrderService extends OrderService {
 *     Long createOrder(Order order, List&lt;Long&gt; orderItemIds);
 *     Boolean payOrder(Long orderId, String paymentMethod);
 *     Boolean cancelOrder(Long orderId, Long memberId);
 *     Order findByOrderNo(String orderNo);
 * }
 *
 * // mortise-edu 中扩展（管理端需要管理功能）
 * public interface EduOrderService extends OrderService {
 *     Page&lt;Order&gt; findOrderList(Page&lt;Order&gt; page, OrderSearch search);
 *     Boolean updateOrderStatus(Long orderId, Integer status);
 *     OrderStatistics getOrderStatistics(LocalDate startDate, LocalDate endDate);
 * }
 * </pre>
 *
 * @author ronger
 */
public interface OrderService extends IService<Order> {
    // 空接口，仅继承 IService 的基础 CRUD 能力
    // 特定业务方法由 edu 和 api 模块各自扩展
}
