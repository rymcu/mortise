package com.rymcu.mortise.member.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rymcu.mortise.member.entity.Enrollment;
import com.rymcu.mortise.member.mapper.EnrollmentMapper;
import com.rymcu.mortise.member.service.EnrollmentService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * 学员报名基础服务实现
 * <p>
 * 仅提供 MyBatis-Flex 的基础 CRUD 实现，不包含业务逻辑
 * <p>
 * 业务模块（edu、api）应继承此类，扩展自己的业务方法
 *
 * @author ronger
 */
@Service
@Primary
public class EnrollmentServiceImpl extends ServiceImpl<EnrollmentMapper, Enrollment> implements EnrollmentService {
    // 空实现，继承 ServiceImpl 即可获得完整的 CRUD 能力
    // 特定业务方法由 edu 和 api 模块的子类实现
}
