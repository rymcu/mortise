# 默认角色唯一性保证 - 快速参考

## 📋 实现概览

```
默认角色唯一性 = 数据库约束 + 应用逻辑 + 查询兜底
```

## 🚀 快速实施指南

### 1️⃣ 数据库迁移（选择对应的数据库）

#### PostgreSQL
```bash
# 执行迁移脚本
psql -U username -d mortise -f docs/sql/V1.0.0__add_role_is_default_field.sql
```

#### MySQL
```bash
# 执行迁移脚本
mysql -u username -p mortise < docs/sql/V1.0.0__add_role_is_default_field_mysql.sql
```

### 2️⃣ 代码已完成修改

✅ `Role.java` - 添加 `isDefault` 字段
✅ `RoleService.java` - 添加 `findDefaultRole()` 方法
✅ `RoleServiceImpl.java` - 实现唯一性校验逻辑
✅ `RegisterHandler.java` - 使用新方法替代硬编码

### 3️⃣ 验证部署

```sql
-- 快速验证
SELECT COUNT(*) FROM mortise.mortise_role WHERE is_default = 1 AND del_flag = 0;
-- 结果应该是 1
```

---

## 🛡️ 三层防护

| 层级 | 位置 | 作用 | 并发安全 |
|------|------|------|----------|
| **数据库层** | 唯一索引/触发器 | 强制约束，防止多个默认角色 | ✅ |
| **应用层** | `RoleServiceImpl.saveRole()` | 业务逻辑校验，自动切换 | ⚠️ |
| **查询层** | `RoleServiceImpl.findDefaultRole()` | `LIMIT 1` 兜底 | ❌ |

---

## 🔍 核心代码片段

### 查询默认角色

```java
@Override
public Role findDefaultRole() {
    QueryWrapper queryWrapper = QueryWrapper.create()
            .select(ROLE.ID, ROLE.LABEL, ROLE.PERMISSION)
            .where(ROLE.IS_DEFAULT.eq(DefaultFlag.YES.getValue()))  // 使用枚举
            .limit(1);
    return mapper.selectOneByQuery(queryWrapper);
}
```

### 保存角色时校验

```java
@Override
@Transactional(rollbackFor = Exception.class)
public Boolean saveRole(Role role) {
    // 如果设置为默认角色，自动将其他默认角色设为非默认
    // 使用 DefaultFlag 枚举替代硬编码的 1
    if (role.getIsDefault() != null && role.getIsDefault() == DefaultFlag.YES.getValue()) {
        validateDefaultRole(role.getId());
    }
    // ... 其他逻辑
}
```

### 注册时使用

```java
@Async
@TransactionalEventListener
public void processRegisterEvent(RegisterEvent registerEvent) {
    // 不再硬编码 "user"，而是查找默认角色
    Role role = roleService.findDefaultRole();
    if (role == null) {
        log.warn("未找到默认角色，用户 {} 注册后未分配角色", registerEvent.getIdUser());
        return;
    }
    // ... 分配角色
}
```

---

## 🎯 不同数据库方案选择

### PostgreSQL（推荐）⭐⭐⭐⭐⭐

```sql
-- 部分唯一索引
CREATE UNIQUE INDEX idx_role_is_default 
ON mortise.mortise_role (is_default) 
WHERE is_default = 1 AND del_flag = 0;
```

**优点：** 性能好、维护简单、并发安全

### MySQL 8.0.13+（推荐）⭐⭐⭐⭐

```sql
-- 函数索引
CREATE UNIQUE INDEX idx_role_is_default 
ON mortise.mortise_role ((CASE WHEN is_default = 1 AND del_flag = 0 THEN 1 ELSE NULL END));
```

**优点：** 性能较好、并发安全

### MySQL 旧版本（兼容方案）⭐⭐⭐

```sql
-- 使用触发器
CREATE TRIGGER trg_role_default_insert
BEFORE INSERT ON mortise.mortise_role
FOR EACH ROW
BEGIN
    IF NEW.is_default = 1 THEN
        UPDATE mortise.mortise_role SET is_default = 0 WHERE is_default = 1 AND del_flag = 0;
    END IF;
END;
```

**优点：** 兼容性好

**缺点：** 维护成本高

---

## 🧪 快速测试

### SQL 测试

```sql
-- 尝试创建第二个默认角色（应该失败或自动处理）
INSERT INTO mortise.mortise_role (label, permission, is_default, status, del_flag)
VALUES ('测试角色', 'test', 1, 0, 0);

-- 验证只有一个默认角色
SELECT label, permission, is_default 
FROM mortise.mortise_role 
WHERE is_default = 1 AND del_flag = 0;
```

### Java 测试

```java
// 注册新用户，检查是否分配了默认角色
Role defaultRole = roleService.findDefaultRole();
assertNotNull(defaultRole);
assertEquals(1, defaultRole.getIsDefault());
```

---

## 📊 对比：优化前 vs 优化后

| 特性 | 优化前 | 优化后 |
|------|--------|--------|
| **角色查找** | `findRoleByPermission("user")` | `findDefaultRole()` |
| **硬编码** | ❌ 存在 | ✅ 消除 |
| **灵活性** | ❌ 需改代码 | ✅ 改数据库 |
| **唯一性保证** | ❌ 无 | ✅ 三层防护 |
| **并发安全** | ⚠️ 低 | ✅ 高 |
| **可维护性** | ⚠️ 低 | ✅ 高 |

---

## 🔧 运维命令

### 检查默认角色

```sql
SELECT id, label, permission, is_default, status
FROM mortise.mortise_role
WHERE del_flag = 0
ORDER BY is_default DESC;
```

### 手动设置默认角色

```sql
-- 先清除所有默认标记
UPDATE mortise.mortise_role SET is_default = 0 WHERE is_default = 1;

-- 设置指定角色为默认
UPDATE mortise.mortise_role 
SET is_default = 1 
WHERE permission = 'user' AND del_flag = 0;
```

### 修复多个默认角色（如果出现）

```sql
-- 只保留第一个创建的默认角色
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

---

## 📝 相关文档

- 📘 [详细迁移指南](./role-default-field-migration.md)
- 🛡️ [唯一性保证策略详解](./role-default-uniqueness-strategy.md)
- 🎯 [DefaultFlag 枚举使用指南](./default-flag-enum-guide.md) ⭐ **新增**
- 🗄️ [PostgreSQL 迁移脚本](./sql/V1.0.0__add_role_is_default_field.sql)
- 🗄️ [MySQL 迁移脚本](./sql/V1.0.0__add_role_is_default_field_mysql.sql)

---

## ⚠️ 注意事项

1. **执行顺序**：先执行数据库迁移，再部署代码
2. **备份数据**：执行前务必备份数据库
3. **测试环境**：先在测试环境验证，再上生产
4. **监控告警**：部署后监控默认角色数量是否为 1
5. **回滚准备**：准备好回滚脚本，以备不时之需

---

## ✅ 部署检查清单

- [ ] 选择合适的数据库方案（索引/触发器）
- [ ] 备份生产数据库
- [ ] 在测试环境执行迁移脚本
- [ ] 验证测试环境功能正常
- [ ] 在生产环境执行迁移脚本
- [ ] 验证默认角色唯一性
- [ ] 部署新版本代码
- [ ] 测试用户注册流程
- [ ] 配置监控告警
- [ ] 更新运维文档

---

## 🆘 常见问题

### Q1: 如果数据库中没有 `permission = 'user'` 的角色怎么办？

**A:** 手动创建一个默认角色或修改迁移脚本，将其他角色设为默认。

```sql
-- 创建默认角色
INSERT INTO mortise.mortise_role (label, permission, is_default, status, del_flag)
VALUES ('普通用户', 'user', 1, 0, 0);
```

### Q2: 如果出现了多个默认角色怎么办？

**A:** 使用修复脚本（见上方"运维命令"部分）。

### Q3: 是否必须使用数据库约束？

**A:** 强烈推荐但不强制。如果是低并发场景，仅使用应用层校验也可以。

### Q4: 如何切换默认角色？

**A:** 通过角色管理界面或直接更新数据库：

```sql
-- 方法 1: 直接 SQL
UPDATE mortise.mortise_role SET is_default = 0 WHERE is_default = 1;
UPDATE mortise.mortise_role SET is_default = 1 WHERE permission = 'new_default';

-- 方法 2: 通过应用层（推荐）
Role role = roleService.findById(newDefaultRoleId);
role.setIsDefault(1);
roleService.saveRole(role);  // 会自动处理旧的默认角色
```

---

**最后更新：** 2025-10-02
