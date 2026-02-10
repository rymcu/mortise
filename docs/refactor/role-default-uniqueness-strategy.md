# 默认角色唯一性保证策略

## 概述

为确保系统中只有一个默认角色，我们采用**三层防护机制**：数据库层、应用层、查询层。

## 🛡️ 三层防护机制

### 第一层：数据库层面（最强约束）⭐

通过数据库唯一索引/约束，在最底层防止多个默认角色的产生。

#### PostgreSQL 方案（推荐）

```sql
-- 部分唯一索引：只对 is_default = 1 且 del_flag = 0 的记录生效
CREATE UNIQUE INDEX idx_role_is_default 
ON mortise.mortise_role (is_default) 
WHERE is_default = 1 AND del_flag = 0;
```

**优点：**
- ✅ 数据库级别的强约束
- ✅ 并发安全，防止竞态条件
- ✅ 性能好，索引开销小
- ✅ 符合 PostgreSQL 标准

#### MySQL 8.0.13+ 方案

```sql
-- 使用函数索引（MySQL 8.0.13+）
CREATE UNIQUE INDEX idx_role_is_default 
ON mortise.mortise_role ((CASE WHEN is_default = 1 AND del_flag = 0 THEN 1 ELSE NULL END));
```

#### MySQL 触发器方案（兼容旧版本）

```sql
DELIMITER $$

-- 插入触发器
CREATE TRIGGER trg_role_default_insert
BEFORE INSERT ON mortise.mortise_role
FOR EACH ROW
BEGIN
    IF NEW.is_default = 1 THEN
        -- 先将其他默认角色设置为非默认
        UPDATE mortise.mortise_role 
        SET is_default = 0 
        WHERE is_default = 1 AND del_flag = 0;
    END IF;
END$$

-- 更新触发器
CREATE TRIGGER trg_role_default_update
BEFORE UPDATE ON mortise.mortise_role
FOR EACH ROW
BEGIN
    IF NEW.is_default = 1 AND (OLD.is_default = 0 OR OLD.is_default IS NULL) THEN
        -- 排除当前记录，将其他默认角色设置为非默认
        UPDATE mortise.mortise_role 
        SET is_default = 0 
        WHERE id != NEW.id AND is_default = 1 AND del_flag = 0;
    END IF;
END$$

DELIMITER ;
```

**优点：**
- ✅ 兼容 MySQL 5.x
- ✅ 自动处理，无需应用层逻辑
- ✅ 并发安全

**缺点：**
- ⚠️ 触发器维护成本高
- ⚠️ 可能影响性能
- ⚠️ 调试困难

---

### 第二层：应用层面（业务逻辑）

在 `RoleServiceImpl.saveRole()` 方法中实现业务逻辑校验。

#### 实现代码

```java
@Override
@Transactional(rollbackFor = Exception.class)
public Boolean saveRole(Role role) {
    // 如果设置为默认角色，需要先检查是否已存在其他默认角色
    if (role.getIsDefault() != null && role.getIsDefault() == 1) {
        validateDefaultRole(role.getId());
    }
    
    // ... 其他保存逻辑
}

/**
 * 验证默认角色的唯一性
 * 如果已存在其他默认角色，则将其设置为非默认
 */
private void validateDefaultRole(Long currentRoleId) {
    QueryWrapper queryWrapper = QueryWrapper.create()
            .where(ROLE.IS_DEFAULT.eq(1));
    
    // 如果是更新操作，排除当前角色
    if (currentRoleId != null) {
        queryWrapper.and(ROLE.ID.ne(currentRoleId));
    }
    
    List<Role> defaultRoles = mapper.selectListByQuery(queryWrapper);
    
    if (!defaultRoles.isEmpty()) {
        // 将其他默认角色设置为非默认
        for (Role existingDefaultRole : defaultRoles) {
            Role updateRole = UpdateEntity.of(Role.class, existingDefaultRole.getId());
            updateRole.setIsDefault(0);
            mapper.update(updateRole);
        }
    }
}
```

**优点：**
- ✅ 业务逻辑清晰
- ✅ 易于测试和维护
- ✅ 可以添加日志和监控
- ✅ 可以自定义处理策略

**缺点：**
- ⚠️ 无法防止直接 SQL 操作
- ⚠️ 在高并发下可能存在竞态条件（需配合数据库层约束）

---

### 第三层：查询层面（兜底保护）

在查询默认角色时使用 `LIMIT 1`，确保只返回一个结果。

#### 实现代码

```java
@Override
public Role findDefaultRole() {
    QueryWrapper queryWrapper = QueryWrapper.create()
            .select(ROLE.ID, ROLE.LABEL, ROLE.PERMISSION)
            .where(ROLE.IS_DEFAULT.eq(1))
            .limit(1);  // 只返回一个
    return mapper.selectOneByQuery(queryWrapper);
}
```

**优点：**
- ✅ 兜底保护，即使存在多个也能正常工作
- ✅ 防御性编程

---

## 📊 方案对比

| 方案 | 安全性 | 性能 | 维护成本 | 并发安全 | 推荐指数 |
|------|--------|------|----------|----------|----------|
| PostgreSQL 部分索引 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ✅ | ⭐⭐⭐⭐⭐ |
| MySQL 函数索引 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ✅ | ⭐⭐⭐⭐ |
| MySQL 触发器 | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐ | ✅ | ⭐⭐⭐ |
| 应用层校验 | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⚠️ | ⭐⭐⭐ |
| 查询层兜底 | ⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ❌ | ⭐⭐ |

---

## 🎯 推荐组合方案

### 方案 A：PostgreSQL 环境（强烈推荐）

```
数据库部分索引 + 应用层校验 + 查询层兜底
```

**执行步骤：**
1. 创建部分唯一索引
2. 应用层已实现校验逻辑
3. 查询层已实现 LIMIT 1

### 方案 B：MySQL 8.0+ 环境

```
数据库函数索引 + 应用层校验 + 查询层兜底
```

### 方案 C：MySQL 旧版本环境

```
数据库触发器 + 应用层校验 + 查询层兜底
```

或者

```
仅应用层校验 + 查询层兜底
```
（适用于低并发场景）

---

## 🧪 测试验证

### 测试用例 1：正常设置默认角色

```java
@Test
public void testSetDefaultRole() {
    Role role = new Role();
    role.setLabel("普通用户");
    role.setPermission("user");
    role.setIsDefault(1);
    
    Boolean result = roleService.saveRole(role);
    
    assertTrue(result);
    // 验证只有一个默认角色
    List<Role> defaultRoles = findAllDefaultRoles();
    assertEquals(1, defaultRoles.size());
}
```

### 测试用例 2：切换默认角色

```java
@Test
public void testSwitchDefaultRole() {
    // 创建第一个默认角色
    Role role1 = createRole("角色1", "role1", 1);
    
    // 创建第二个默认角色（应该自动将第一个设为非默认）
    Role role2 = createRole("角色2", "role2", 1);
    
    // 验证只有 role2 是默认角色
    Role defaultRole = roleService.findDefaultRole();
    assertEquals(role2.getId(), defaultRole.getId());
    
    // 验证 role1 已不是默认角色
    Role reloadedRole1 = roleService.findById(role1.getId());
    assertEquals(0, reloadedRole1.getIsDefault());
}
```

### 测试用例 3：并发场景测试

```java
@Test
public void testConcurrentSetDefaultRole() throws InterruptedException {
    int threadCount = 10;
    CountDownLatch latch = new CountDownLatch(threadCount);
    
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    
    for (int i = 0; i < threadCount; i++) {
        final int index = i;
        executor.submit(() -> {
            try {
                Role role = new Role();
                role.setLabel("角色" + index);
                role.setPermission("role" + index);
                role.setIsDefault(1);
                roleService.saveRole(role);
            } finally {
                latch.countDown();
            }
        });
    }
    
    latch.await();
    
    // 验证最终只有一个默认角色
    List<Role> defaultRoles = findAllDefaultRoles();
    assertEquals(1, defaultRoles.size());
}
```

---

## 📝 运维建议

### 定期检查脚本

```sql
-- 检查是否存在多个默认角色
SELECT COUNT(*) as default_role_count
FROM mortise.mortise_role
WHERE is_default = 1 AND del_flag = 0;

-- 如果发现多个，手动修复
UPDATE mortise.mortise_role
SET is_default = 0
WHERE is_default = 1 AND del_flag = 0
  AND id NOT IN (
    SELECT id FROM mortise.mortise_role
    WHERE is_default = 1 AND del_flag = 0
    ORDER BY created_time ASC
    LIMIT 1
  );
```

### 监控告警

在应用监控中添加指标：
- `default_role_count`：默认角色数量，期望值为 1
- 如果 > 1，触发告警

---

## 🔄 迁移检查清单

- [ ] 选择适合的数据库方案（索引/触发器）
- [ ] 执行数据库迁移脚本
- [ ] 验证唯一索引/触发器已生效
- [ ] 部署包含应用层校验的代码
- [ ] 执行单元测试
- [ ] 执行并发测试
- [ ] 配置监控告警
- [ ] 准备运维检查脚本

---

## 总结

通过**三层防护**机制，我们确保了默认角色的唯一性：

1. **数据库层**：提供最强约束，防止数据不一致
2. **应用层**：实现业务逻辑，提供灵活性
3. **查询层**：兜底保护，确保系统稳定运行

推荐使用 **PostgreSQL 部分索引 + 应用层校验 + 查询层兜底** 的组合方案，既保证了数据一致性，又提供了良好的可维护性。
