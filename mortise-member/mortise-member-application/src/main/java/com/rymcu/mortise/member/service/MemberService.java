package com.rymcu.mortise.member.service;

import com.mybatisflex.core.service.IService;
import com.rymcu.mortise.member.entity.Member;

/**
 * 会员基础服务
 * <p>
 * 提供会员的基础 CRUD 操作，仅包含真正通用的查询方法
 * <p>
 * 继承 IService 自动获得以下能力：
 * - save(), saveBatch() - 新增
 * - updateById(), update() - 更新
 * - removeById(), remove() - 删除
 * - getById(), list(), page() - 查询
 * <p>
 * 业务场景说明：
 * - 管理端（mortise-edu）：需要列表查询、状态管理（启用/禁用）等功能
 * - 客户端（mortise-api）：需要登录相关（findByUsername/Email/Phone）、密码管理等功能
 * <p>
 * 设计原则：
 * - ✅ 本接口仅提供最基础的 CRUD（继承自 IService）
 * - ✅ 特定业务场景的方法应在各业务模块中扩展实现
 * - ❌ 不在此接口中定义特定场景的业务方法
 * <p>
 * 扩展示例：
 * <pre>
 * // mortise-api 中扩展（客户端需要）
 * public interface ApiMemberService extends MemberService {
 *     Member findByUsername(String username);
 *     Member findByEmail(String email);
 *     Long register(Member member, String password);
 * }
 *
 * // mortise-edu 中扩展（管理端需要）
 * public interface EduMemberService extends MemberService {
 *     Page&lt;Member&gt; findMemberList(Page&lt;Member&gt; page, MemberSearch search);
 *     Boolean enableMember(Long id);
 *     Boolean disableMember(Long id);
 * }
 * </pre>
 *
 * @author ronger
 */
public interface MemberService extends IService<Member> {
    // 空接口，仅继承 IService 的基础 CRUD 能力
    // 特定业务方法由 edu 和 api 模块各自扩展
}
