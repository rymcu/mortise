package com.rymcu.mortise.member.service;

import com.mybatisflex.core.service.IService;
import com.rymcu.mortise.member.entity.Enrollment;

/**
 * 学员报名基础服务
 * <p>
 * 提供学员报名的基础 CRUD 操作
 * <p>
 * 业务场景说明：
 * - 管理端（mortise-edu）：需要报名列表查询、报名审核、状态管理等功能
 * - 客户端（mortise-api）：需要课程报名、报名查询、取消报名等功能
 * <p>
 * 设计原则：
 * - ✅ 本接口仅提供最基础的 CRUD（继承自 IService）
 * - ✅ 特定业务场景的方法应在各业务模块中扩展实现
 * - ❌ 不在此接口中定义特定场景的业务方法
 * <p>
 * 扩展示例：
 * <pre>
 * // mortise-api 中扩展（客户端需要）
 * public interface ApiEnrollmentService extends EnrollmentService {
 *     Long createEnrollment(Enrollment enrollment);
 *     Boolean cancelEnrollment(Long enrollmentId, Long memberId);
 *     Enrollment findByMemberAndCourse(Long memberId, Long courseId);
 * }
 *
 * // mortise-edu 中扩展（管理端需要）
 * public interface EduEnrollmentService extends EnrollmentService {
 *     Page&lt;Enrollment&gt; findEnrollmentList(Page&lt;Enrollment&gt; page, EnrollmentSearch search);
 *     Boolean approveEnrollment(Long enrollmentId);
 *     Boolean rejectEnrollment(Long enrollmentId, String reason);
 * }
 * </pre>
 *
 * @author ronger
 */
public interface EnrollmentService extends IService<Enrollment> {
    // 空接口，仅继承 IService 的基础 CRUD 能力
    // 特定业务方法由 edu 和 api 模块各自扩展
}
