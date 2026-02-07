package com.rymcu.mortise.member.service;

import com.mybatisflex.core.service.IService;
import com.rymcu.mortise.member.entity.RefundRequest;

/**
 * 退款申请基础服务接口
 * <p>
 * 提供退款申请的基础 CRUD 操作（通过继承 MyBatis-Flex IService）
 * 具体业务逻辑由各模块扩展实现
 * </p>
 *
 * @author ronger
 */
public interface RefundRequestService extends IService<RefundRequest> {
}
