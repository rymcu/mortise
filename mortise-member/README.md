# Mortise Member Module

## 模块说明

`mortise-member` 是会员和商品的共享模块，提供核心实体、Mapper 和服务层接口，供 `mortise-edu` 和 `mortise-api` 模块共同使用。

## 模块职责

### 核心实体
- ✅ **Member** - 会员实体
- ✅ **Product** - 商品实体  
- ✅ **ProductCategory** - 商品分类实体

### 数据访问层
- ✅ **MemberMapper** - 会员数据访问
- ✅ **ProductMapper** - 商品数据访问
- ✅ **ProductCategoryMapper** - 商品分类数据访问

### 服务层
- ✅ **基础服务接口**（仅继承 IService，提供 CRUD 能力）
- ✅ **基础服务实现**（空实现，继承 ServiceImpl）
- ✅ **业务模块扩展**（edu、api 模块根据需求扩展）

## 模块结构

```
mortise-member/
├── src/main/java/com/rymcu/mortise/member/
│   ├── entity/              # 实体类
│   │   ├── Member.java
│   │   ├── Product.java
│   │   ├── Order.java
│   │   ├── Enrollment.java
│   │   └── ...
│   ├── mapper/              # MyBatis Mapper
│   │   ├── MemberMapper.java
│   │   ├── ProductMapper.java
│   │   ├── OrderMapper.java
│   │   └── ...
│   ├── service/             # 基础服务接口（仅继承 IService）
│   │   ├── MemberService.java
│   │   ├── ProductService.java
│   │   ├── OrderService.java
│   │   └── EnrollmentService.java
│   ├── service/impl/        # 基础服务实现（空实现）
│   │   ├── MemberServiceImpl.java
│   │   ├── ProductServiceImpl.java
│   │   ├── OrderServiceImpl.java
│   │   └── EnrollmentServiceImpl.java
│   ├── model/               # DTO/VO
│   └── enums/               # 枚举类
├── SERVICE_EXTENSION_GUIDE.md  # 服务扩展指南（重要）
└── pom.xml
```

## 依赖关系

### 本模块依赖
- `mortise-core` - 核心工具和通用组件
- `mortise-persistence` - 持久化层

### 被以下模块依赖
- `mortise-edu` - 教育业务模块
- `mortise-api` - 商城业务模块

## 使用方式

### 1. 在 edu 或 api 模块的 pom.xml 中添加依赖

```xml
<dependency>
    <groupId>com.rymcu</groupId>
    <artifactId>mortise-member</artifactId>
</dependency>
```

### 2. 使用实体类和 Mapper

```java
import com.rymcu.mortise.member.entity.Product;
import com.rymcu.mortise.member.entity.Member;
import com.rymcu.mortise.member.entity.Order;

import com.rymcu.mortise.member.mapper.ProductMapper;
import com.rymcu.mortise.member.mapper.MemberMapper;
import com.rymcu.mortise.member.mapper.OrderMapper;
```

### 3. 扩展基础服务（重要）⭐

**基础服务设计原则**：
- ✅ mortise-member 仅提供最基础的 CRUD（继承 MyBatis-Flex 的 IService）
- ✅ 特定业务场景的方法应在各业务模块（edu、api）中扩展实现
- ❌ 不在基础服务中定义特定场景的业务方法

#### 方式 A：直接使用 Mapper（适用于简单查询）

```java
@Service
@RequiredArgsConstructor
public class CourseServiceImpl {
    private final MemberMapper memberMapper;
    private final OrderMapper orderMapper;
    
    public void someMethod() {
        // 直接使用 Mapper 进行查询
        Member member = memberMapper.selectOneById(memberId);
        List<Order> orders = orderMapper.selectListByQuery(
            QueryWrapper.create().where(ORDER.MEMBER_ID.eq(memberId))
        );
    }
}
```

#### 方式 B：扩展基础服务（适用于复杂业务逻辑）

**客户端（mortise-api）扩展示例**：

```java
// 1. 定义客户端会员服务接口
package com.rymcu.mortise.api.service;

import com.rymcu.mortise.member.entity.Member;

public interface MemberService extends com.rymcu.mortise.member.service.MemberService {
    // 扩展客户端特定的方法
    Member findByUsername(String username);
    Member findByEmail(String email);
    Long register(Member member, String password);
    Member login(String account, String password);
    Boolean updatePassword(Long memberId, String oldPassword, String newPassword);
}

// 2. 实现客户端会员服务
package com.rymcu.mortise.api.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.stereotype.Service;

import static com.rymcu.mortise.member.entity.table.MemberTableDef.MEMBER;

@Service
public class MemberServiceImpl 
    extends com.rymcu.mortise.member.service.impl.MemberServiceImpl 
    implements com.rymcu.mortise.api.service.MemberService {

    private final PasswordEncoder passwordEncoder;
    
    @Override
    public Member findByUsername(String username) {
        return getOne(QueryWrapper.create().where(MEMBER.USERNAME.eq(username)));
    }
    
    @Override
    public Long register(Member member, String password) {
        member.setPasswordHash(passwordEncoder.encode(password));
        save(member); // 使用继承自 ServiceImpl 的方法
        return member.getId();
    }
    
    // ... 其他客户端特定的业务方法
}
```

**管理端（mortise-edu）扩展示例**：

```java
// 1. 定义管理端会员服务接口
package com.rymcu.mortise.edu.service;

import com.mybatisflex.core.paginate.Page;
import com.rymcu.mortise.member.entity.Member;

public interface MemberService extends com.rymcu.mortise.member.service.MemberService {
    // 扩展管理端特定的方法
    Page<Member> findMemberList(Page<Member> page, MemberSearch search);
    Boolean enableMember(Long memberId);
    Boolean disableMember(Long memberId);
    MemberStatistics getMemberStatistics();
}

// 2. 实现管理端会员服务
package com.rymcu.mortise.edu.service.impl;

import org.springframework.stereotype.Service;

@Service
public class MemberServiceImpl 
    extends com.rymcu.mortise.member.service.impl.MemberServiceImpl 
    implements com.rymcu.mortise.edu.service.MemberService {

    @Override
    public Page<Member> findMemberList(Page<Member> page, MemberSearch search) {
        // 实现列表查询逻辑
    }
    
    @Override
    public Boolean enableMember(Long memberId) {
        // 实现启用会员逻辑
    }
    
    // ... 其他管理端特定的业务方法
}
```

**详细扩展示例请参考**：[SERVICE_EXTENSION_GUIDE.md](SERVICE_EXTENSION_GUIDE.md)

## 设计原则

1. **单一职责**：仅包含会员和商品相关的核心实体和基础服务
2. **最小化基础服务**：基础服务仅提供 CRUD 能力，不包含业务逻辑
3. **低耦合**：不依赖任何业务模块（edu、api 等）
4. **高内聚**：将共享的会员和商品功能集中管理
5. **可扩展**：业务模块（edu、api）根据各自需求扩展服务层

## 注意事项

1. **不要在此模块中添加业务逻辑**：复杂的业务逻辑应该在 edu 或 api 模块中实现
2. **基础服务为空接口和空实现**：仅继承 MyBatis-Flex 的 IService 和 ServiceImpl
3. **保持实体的通用性**：实体设计应满足多个模块的需求
4. **避免循环依赖**：此模块不应该依赖 edu 或 api 模块
5. **区分管理端和客户端需求**：
   - 管理端（edu）：列表查询、状态管理、审核、统计等
   - 客户端（api）：登录认证、订单流程、支付、购物车等

## 业务场景区分

### Member（会员）
- **客户端（api）**：注册、登录、密码管理、个人信息修改
- **管理端（edu）**：会员列表查询、启用/禁用、信息编辑、统计分析

### Order（订单）
- **客户端（api）**：创建订单、支付、取消、确认收货
- **管理端（edu）**：订单列表查询、状态变更、统计报表

### Product（商品）
- **客户端（api）**：商品浏览、搜索、详情查询（仅已上架）
- **管理端（edu）**：商品 CRUD、上下架、分类管理、库存管理

### Enrollment（报名）
- **客户端（api）**：课程报名、取消报名、报名查询
- **管理端（edu）**：报名列表、审核、状态管理、统计分析

## 后续规划

- [x] 添加基础 Service 接口（MemberService、OrderService 等）
- [x] 添加基础 ServiceImpl 实现（空实现，仅继承 ServiceImpl）
- [x] 编写服务扩展指南（SERVICE_EXTENSION_GUIDE.md）
- [ ] 在 edu 模块中实现管理端服务扩展
- [ ] 在 api 模块中实现客户端服务扩展
- [ ] 完善 DTO/VO 模型
- [ ] 完善枚举类（ProductStatus、MemberLevel 等）
