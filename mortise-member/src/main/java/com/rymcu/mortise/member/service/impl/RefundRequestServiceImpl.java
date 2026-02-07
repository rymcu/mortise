package com.rymcu.mortise.member.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rymcu.mortise.member.entity.RefundRequest;
import com.rymcu.mortise.member.mapper.RefundRequestMapper;
import com.rymcu.mortise.member.service.RefundRequestService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * 退款申请基础服务实现
 * <p>
 * 提供 MyBatis-Flex 的基础 CRUD 能力
 * 不包含具体业务逻辑
 * </p>
 *
 * @author ronger
 */
@Service
@Primary
public class RefundRequestServiceImpl
        extends ServiceImpl<RefundRequestMapper, RefundRequest>
        implements RefundRequestService {
}
