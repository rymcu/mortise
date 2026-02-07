# Mortise Member 基础服务层扩展指南

## 📐 设计原则

### 核心思想：**最小化基础服务，业务逻辑下沉到业务模块**

```
mortise-member (基础服务层)
    ├── 职责：提供基础 CRUD 能力（继承 MyBatis-Flex 的 IService）
    ├── 原则：仅包含真正通用的、两端都需要的功能
    └── 实现：空接口 + 空实现（继承 ServiceImpl 即可）

mortise-edu (管理端业务层)
    ├── 职责：管理端特定的业务逻辑
    ├── 特性：列表查询、状态管理、审核、统计等
    └── 实现：扩展 member 的基础服务，添加管理端方法

mortise-api (客户端业务层)
    ├── 职责：客户端特定的业务逻辑
    ├── 特性：登录认证、订单流程、支付、购物车等
    └── 实现：扩展 member 的基础服务，添加客户端方法
```

---

## 🎯 业务场景区分

### 1. Member（会员）服务

#### 客户端（mortise-api）需要：
- ✅ 注册登录相关：`findByUsername`, `findByEmail`, `findByPhone`
- ✅ 密码管理：`register`, `login`, `updatePassword`, `resetPassword`
- ✅ 个人信息：`updateProfile`, `verifyEmail`, `verifyPhone`

#### 管理端（mortise-edu）需要：
- ✅ 列表查询：`findMemberList`（支持分页、排序、筛选）
- ✅ 状态管理：`enableMember`, `disableMember`
- ✅ 信息编辑：`updateMemberInfo`
- ✅ 统计分析：`getMemberStatistics`

---

### 2. Order（订单）服务

#### 客户端（mortise-api）需要：
- ✅ 订单创建：`createOrder`, `calculateOrderAmount`
- ✅ 订单支付：`payOrder`, `confirmPayment`
- ✅ 订单操作：`cancelOrder`, `confirmReceipt`
- ✅ 订单查询：`findByOrderNo`, `findMyOrders`

#### 管理端（mortise-edu）需要：
- ✅ 列表查询：`findOrderList`（支持多条件筛选）
- ✅ 状态管理：`updateOrderStatus`, `processRefund`
- ✅ 订单详情：`getOrderDetail`（包含完整信息）
- ✅ 统计报表：`getOrderStatistics`, `getSalesReport`

---

### 3. Product（商品）服务

#### 客户端（mortise-api）需要：
- ✅ 商品浏览：`findPublishedProducts`（仅查询已上架）
- ✅ 商品详情：`getProductDetail`
- ✅ 搜索筛选：`searchProducts`, `findByCategory`
- ✅ 浏览统计：`incrementViewCount`

#### 管理端（mortise-edu）需要：
- ✅ 完整 CRUD：`createProduct`, `updateProduct`, `deleteProduct`
- ✅ 状态管理：`publishProduct`, `onSale`, `offSale`
- ✅ 列表查询：`findProductList`（包含所有状态）
- ✅ 批量操作：`batchUpdateStatus`, `batchDelete`

---

### 4. Enrollment（报名）服务

#### 客户端（mortise-api）需要：
- ✅ 报名操作：`createEnrollment`, `cancelEnrollment`
- ✅ 报名查询：`findByMemberAndCourse`, `findMyEnrollments`
- ✅ 进度管理：`updateProgress`, `completeEnrollment`

#### 管理端（mortise-edu）需要：
- ✅ 列表查询：`findEnrollmentList`（多维度筛选）
- ✅ 审核管理：`approveEnrollment`, `rejectEnrollment`
- ✅ 状态管理：`activateEnrollment`, `suspendEnrollment`
- ✅ 统计报表：`getEnrollmentStatistics`

---

## 💡 扩展实现示例

### 示例 1: 客户端会员服务（mortise-api）

```java
// mortise-api/src/main/java/com/rymcu/mortise/api/service/MemberService.java
package com.rymcu.mortise.api.service;

import com.rymcu.mortise.member.entity.Member;

/**
 * 客户端会员服务
 * <p>
 * 扩展基础会员服务，添加客户端特定的业务方法
 */
public interface MemberService extends com.rymcu.mortise.member.service.MemberService {

    /**
     * 根据用户名查询会员（用于登录）
     */
    Member findByUsername(String username);

    /**
     * 根据邮箱查询会员（用于登录）
     */
    Member findByEmail(String email);

    /**
     * 根据手机号查询会员（用于登录）
     */
    Member findByPhone(String phone);

    /**
     * 会员注册
     */
    Long register(Member member, String password);

    /**
     * 会员登录（用户名/邮箱/手机号）
     */
    Member login(String account, String password);

    /**
     * 手机号验证码登录
     */
    Member loginByPhone(String phone, String code);

    /**
     * 修改密码
     */
    Boolean updatePassword(Long memberId, String oldPassword, String newPassword);

    /**
     * 重置密码
     */
    Boolean resetPassword(String account, String newPassword, String verificationCode);

    /**
     * 邮箱验证
     */
    Boolean verifyEmail(Long memberId, String code);

    /**
     * 手机号验证
     */
    Boolean verifyPhone(Long memberId, String code);

    /**
     * 更新最后登录时间
     */
    void updateLastLoginTime(Long memberId);
}
```

```java
// mortise-api/src/main/java/com/rymcu/mortise/api/service/impl/MemberServiceImpl.java
package com.rymcu.mortise.api.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.rymcu.mortise.member.entity.Member;
import com.rymcu.mortise.member.mapper.MemberMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.rymcu.mortise.member.entity.table.MemberTableDef.MEMBER;

/**
 * 客户端会员服务实现
 * <p>
 * 继承 member 模块的基础实现，扩展客户端特定的业务逻辑
 */
@Service
public class MemberServiceImpl 
    extends com.rymcu.mortise.member.service.impl.MemberServiceImpl 
    implements com.rymcu.mortise.api.service.MemberService {

    private final PasswordEncoder passwordEncoder;
    private final VerificationCodeService verificationCodeService;

    public MemberServiceImpl(PasswordEncoder passwordEncoder, 
                            VerificationCodeService verificationCodeService) {
        this.passwordEncoder = passwordEncoder;
        this.verificationCodeService = verificationCodeService;
    }

    @Override
    public Member findByUsername(String username) {
        return getOne(QueryWrapper.create()
                .where(MEMBER.USERNAME.eq(username))
        );
    }

    @Override
    public Member findByEmail(String email) {
        return getOne(QueryWrapper.create()
                .where(MEMBER.EMAIL.eq(email))
        );
    }

    @Override
    public Member findByPhone(String phone) {
        return getOne(QueryWrapper.create()
                .where(MEMBER.PHONE.eq(phone))
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(Member member, String password) {
        // 验证用户名是否已存在
        if (findByUsername(member.getUsername()) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }

        // 加密密码
        member.setPasswordHash(passwordEncoder.encode(password));
        member.setStatus(0); // 正常状态
        member.setMemberLevel("normal");
        member.setPoints(0);

        // 保存会员（使用继承自 ServiceImpl 的方法）
        save(member);
        return member.getId();
    }

    @Override
    public Member login(String account, String password) {
        // 尝试通过用户名、邮箱或手机号查找
        Member member = findByUsername(account);
        if (member == null) {
            member = findByEmail(account);
        }
        if (member == null) {
            member = findByPhone(account);
        }

        if (member == null) {
            throw new IllegalArgumentException("账号不存在");
        }

        // 验证密码
        if (!passwordEncoder.matches(password, member.getPasswordHash())) {
            throw new IllegalArgumentException("密码错误");
        }

        // 更新最后登录时间
        updateLastLoginTime(member.getId());

        return member;
    }

    @Override
    public Member loginByPhone(String phone, String code) {
        // 验证验证码
        if (!verificationCodeService.verifySmsCode(phone, code)) {
            throw new IllegalArgumentException("验证码错误或已过期");
        }

        Member member = findByPhone(phone);
        if (member == null) {
            throw new IllegalArgumentException("手机号未注册");
        }

        updateLastLoginTime(member.getId());
        return member;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updatePassword(Long memberId, String oldPassword, String newPassword) {
        Member member = getById(memberId);
        if (member == null) {
            throw new IllegalArgumentException("会员不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, member.getPasswordHash())) {
            throw new IllegalArgumentException("原密码错误");
        }

        // 更新新密码
        member.setPasswordHash(passwordEncoder.encode(newPassword));
        return updateById(member);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean resetPassword(String account, String newPassword, String verificationCode) {
        // 验证验证码（此处简化，实际应从 Redis 验证）
        // TODO: 实现验证码验证逻辑

        Member member = findByUsername(account);
        if (member == null) {
            member = findByEmail(account);
        }
        if (member == null) {
            member = findByPhone(account);
        }

        if (member == null) {
            throw new IllegalArgumentException("账号不存在");
        }

        member.setPasswordHash(passwordEncoder.encode(newPassword));
        return updateById(member);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean verifyEmail(Long memberId, String code) {
        Member member = getById(memberId);
        if (member == null) {
            throw new IllegalArgumentException("会员不存在");
        }

        // 验证邮箱验证码
        if (!verificationCodeService.verifyEmailCode(member.getEmail(), code)) {
            throw new IllegalArgumentException("验证码错误或已过期");
        }

        // 标记邮箱已验证
        member.setEmailVerified(true);
        return updateById(member);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean verifyPhone(Long memberId, String code) {
        Member member = getById(memberId);
        if (member == null) {
            throw new IllegalArgumentException("会员不存在");
        }

        // 验证手机验证码
        if (!verificationCodeService.verifySmsCode(member.getPhone(), code)) {
            throw new IllegalArgumentException("验证码错误或已过期");
        }

        // 标记手机已验证
        member.setPhoneVerified(true);
        return updateById(member);
    }

    @Override
    public void updateLastLoginTime(Long memberId) {
        // 使用 MyBatis-Flex 的更新方法
        Member member = new Member();
        member.setId(memberId);
        member.setLastLoginTime(LocalDateTime.now());
        updateById(member);
    }
}
```

---

### 示例 2: 管理端会员服务（mortise-edu）

```java
// mortise-edu/src/main/java/com/rymcu/mortise/edu/service/MemberService.java
package com.rymcu.mortise.edu.service;

import com.mybatisflex.core.paginate.Page;
import com.rymcu.mortise.member.entity.Member;
import com.rymcu.mortise.edu.model.MemberSearch;

/**
 * 管理端会员服务
 * <p>
 * 扩展基础会员服务，添加管理端特定的业务方法
 */
public interface MemberService extends com.rymcu.mortise.member.service.MemberService {

    /**
     * 分页查询会员列表（支持多条件筛选）
     */
    Page<Member> findMemberList(Page<Member> page, MemberSearch search);

    /**
     * 启用会员
     */
    Boolean enableMember(Long memberId);

    /**
     * 禁用会员
     */
    Boolean disableMember(Long memberId);

    /**
     * 批量启用会员
     */
    Boolean batchEnable(List<Long> memberIds);

    /**
     * 批量禁用会员
     */
    Boolean batchDisable(List<Long> memberIds);

    /**
     * 获取会员统计信息
     */
    MemberStatistics getMemberStatistics(LocalDate startDate, LocalDate endDate);
}
```

```java
// mortise-edu/src/main/java/com/rymcu/mortise/edu/service/impl/MemberServiceImpl.java
package com.rymcu.mortise.edu.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.rymcu.mortise.member.entity.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.rymcu.mortise.member.entity.table.MemberTableDef.MEMBER;

/**
 * 管理端会员服务实现
 */
@Service
public class MemberServiceImpl 
    extends com.rymcu.mortise.member.service.impl.MemberServiceImpl 
    implements com.rymcu.mortise.edu.service.MemberService {

    @Override
    public Page<Member> findMemberList(Page<Member> page, MemberSearch search) {
        QueryWrapper query = QueryWrapper.create()
                .from(MEMBER);

        // 用户名筛选
        if (StringUtils.isNotBlank(search.getUsername())) {
            query.where(MEMBER.USERNAME.like(search.getUsername()));
        }

        // 邮箱筛选
        if (StringUtils.isNotBlank(search.getEmail())) {
            query.where(MEMBER.EMAIL.like(search.getEmail()));
        }

        // 状态筛选
        if (search.getStatus() != null) {
            query.where(MEMBER.STATUS.eq(search.getStatus()));
        }

        // 会员等级筛选
        if (StringUtils.isNotBlank(search.getMemberLevel())) {
            query.where(MEMBER.MEMBER_LEVEL.eq(search.getMemberLevel()));
        }

        // 注册时间范围
        if (search.getStartDate() != null) {
            query.where(MEMBER.CREATED_TIME.ge(search.getStartDate()));
        }
        if (search.getEndDate() != null) {
            query.where(MEMBER.CREATED_TIME.le(search.getEndDate()));
        }

        // 排序
        query.orderBy(MEMBER.CREATED_TIME.desc());

        return mapper.paginate(page, query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean enableMember(Long memberId) {
        Member member = new Member();
        member.setId(memberId);
        member.setStatus(0); // 0-正常
        return updateById(member);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean disableMember(Long memberId) {
        Member member = new Member();
        member.setId(memberId);
        member.setStatus(1); // 1-禁用
        return updateById(member);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchEnable(List<Long> memberIds) {
        // 使用 MyBatis-Flex 的批量更新
        return updateChain()
                .set(MEMBER.STATUS, 0)
                .where(MEMBER.ID.in(memberIds))
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchDisable(List<Long> memberIds) {
        return updateChain()
                .set(MEMBER.STATUS, 1)
                .where(MEMBER.ID.in(memberIds))
                .update();
    }

    @Override
    public MemberStatistics getMemberStatistics(LocalDate startDate, LocalDate endDate) {
        // 实现统计逻辑
        // 可以使用 MyBatis-Flex 的聚合查询
        MemberStatistics stats = new MemberStatistics();
        
        // 总会员数
        stats.setTotalCount(count());
        
        // 活跃会员数
        stats.setActiveCount(countByStatus(0));
        
        // 新注册会员数（指定时间范围）
        stats.setNewCount(countByDateRange(startDate, endDate));
        
        return stats;
    }
}
```

---

## 📝 总结

### ✅ 新架构的优势

1. **职责清晰**：member 模块只提供基础 CRUD，不掺杂业务逻辑
2. **避免冗余**：不在基础层定义特定场景的方法
3. **灵活扩展**：edu 和 api 模块根据自己的需求独立扩展
4. **易于维护**：业务逻辑在各自模块中，方便修改和测试
5. **符合原则**：遵循单一职责、开闭原则

### ⚠️ 注意事项

1. **不要在 member 模块中添加业务逻辑**
2. **edu 和 api 的 Service 接口名称可以相同**（包名不同）
3. **使用继承时注意避免循环依赖**
4. **充分利用 MyBatis-Flex 的 IService 能力**

### 🎯 最佳实践

```java
// ✅ 推荐：基础服务为空接口
public interface MemberService extends IService<Member> {
    // 空接口，仅继承 IService
}

// ✅ 推荐：业务模块扩展
public interface ApiMemberService extends MemberService {
    Member findByUsername(String username);
    Long register(Member member, String password);
}

// ❌ 不推荐：在基础服务中定义特定业务方法
public interface MemberService extends IService<Member> {
    Member findByUsername(String username); // 这是客户端特有的需求
}
```
