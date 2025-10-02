# Role 表添加默认角色字段迁移指南

## 概述

为了优化角色分配逻辑，消除硬编码的 `"user"` 权限字符串，在 `mortise_role` 表中添加 `is_default` 字段。

## 变更内容

### 1. 数据库变更

#### 添加字段
```sql
-- 添加 is_default 字段
ALTER TABLE mortise.mortise_role 
ADD COLUMN is_default INT DEFAULT 0 COMMENT '是否为默认角色（注册时自动分配）0-否，1-是';
```

#### 设置默认角色
```sql
-- 将 permission 为 'user' 的角色设置为默认角色
UPDATE mortise.mortise_role 
SET is_default = 1 
WHERE permission = 'user' AND del_flag = 0;
```

#### 添加唯一索引（**强烈推荐**，数据库层面确保只有一个默认角色）

**PostgreSQL 方式（推荐）：**
```sql
-- 为 is_default 字段创建部分唯一索引（仅对 is_default = 1 的记录）
CREATE UNIQUE INDEX idx_role_is_default 
ON mortise.mortise_role (is_default) 
WHERE is_default = 1 AND del_flag = 0;
```

**MySQL 方式（如果使用 MySQL 8.0+）：**
```sql
-- MySQL 8.0.13+ 支持函数索引
CREATE UNIQUE INDEX idx_role_is_default 
ON mortise.mortise_role ((CASE WHEN is_default = 1 AND del_flag = 0 THEN 1 ELSE NULL END));
```

**MySQL 传统方式（兼容所有版本）：**
```sql
-- 使用触发器保证唯一性（见下方触发器方案）
```

### 2. 代码变更

#### 实体类变更
- `Role.java`：添加 `isDefault` 字段

#### 服务层变更
- `RoleService.java`：添加 `findDefaultRole()` 方法
- `RoleServiceImpl.java`：实现 `findDefaultRole()` 方法

#### 业务逻辑变更
- `RegisterHandler.java`：使用 `findDefaultRole()` 替代 `findRoleByPermission("user")`

## 优势

1. **消除硬编码**：不再依赖魔法字符串 `"user"`
2. **更灵活**：可以通过数据库配置轻松更改默认角色
3. **更清晰**：语义明确，`is_default` 一目了然
4. **更安全**：添加了空值检查，避免默认角色不存在时的异常
5. **易于维护**：修改默认角色只需更新数据库，无需修改代码

## 回滚方案

如果需要回滚：

```sql
-- 删除字段
ALTER TABLE mortise.mortise_role DROP COLUMN is_default;

-- 删除索引（如果创建了）
DROP INDEX IF EXISTS idx_role_is_default ON mortise.mortise_role;
```

代码层面恢复到使用 `findRoleByPermission("user")` 的方式。

## 唯一性保证机制

本方案采用**三层防护**确保默认角色的唯一性：

### 1️⃣ 数据库层面（最强约束）
通过唯一索引在数据库层面强制约束，**防止并发情况下产生多个默认角色**。

### 2️⃣ 应用层面（业务逻辑）
在 `RoleServiceImpl.saveRole()` 方法中：
- 保存/更新角色时，如果设置 `is_default = 1`
- 自动将其他默认角色的 `is_default` 设置为 0
- 确保业务逻辑的一致性

### 3️⃣ 查询层面（兜底保护）
`findDefaultRole()` 使用 `LIMIT 1`，即使存在多个默认角色也只返回一个。

## MySQL 触发器方案（备选）

如果使用 MySQL 且无法使用部分索引，可以使用触发器：

```sql
-- 创建触发器：插入时自动处理
DELIMITER $$
CREATE TRIGGER trg_role_default_insert
BEFORE INSERT ON mortise.mortise_role
FOR EACH ROW
BEGIN
    IF NEW.is_default = 1 THEN
        UPDATE mortise.mortise_role 
        SET is_default = 0 
        WHERE is_default = 1 AND del_flag = 0;
    END IF;
END$$

-- 创建触发器：更新时自动处理
CREATE TRIGGER trg_role_default_update
BEFORE UPDATE ON mortise.mortise_role
FOR EACH ROW
BEGIN
    IF NEW.is_default = 1 AND (OLD.is_default = 0 OR OLD.is_default IS NULL) THEN
        UPDATE mortise.mortise_role 
        SET is_default = 0 
        WHERE id != NEW.id AND is_default = 1 AND del_flag = 0;
    END IF;
END$$
DELIMITER ;
```

## 注意事项

1. **执行顺序**：先执行数据库迁移，再部署新代码
2. **默认角色检查**：确保至少有一个角色的 `is_default = 1`
3. **唯一性保证**：
   - **PostgreSQL**：使用部分唯一索引（推荐）
   - **MySQL 8.0+**：使用函数索引
   - **MySQL 旧版本**：使用触发器或仅依赖应用层逻辑
4. **日志监控**：注意监控 `RegisterHandler` 中的警告日志，确保默认角色存在
5. **并发安全**：数据库唯一索引可防止并发插入时产生多个默认角色

## 🧪 测试验证

### 1. 数据库层测试

```sql
-- 测试 1: 验证索引已创建
SELECT indexname, indexdef 
FROM pg_indexes 
WHERE tablename = 'mortise_role' AND indexname = 'idx_role_is_default';

-- 测试 2: 尝试插入多个默认角色（应该失败）
INSERT INTO mortise.mortise_role (label, permission, is_default, status, del_flag)
VALUES ('测试角色1', 'test1', 1, 0, 0);

INSERT INTO mortise.mortise_role (label, permission, is_default, status, del_flag)
VALUES ('测试角色2', 'test2', 1, 0, 0);
-- 上面第二条应该失败，提示违反唯一约束

-- 测试 3: 验证当前默认角色数量
SELECT COUNT(*) FROM mortise.mortise_role 
WHERE is_default = 1 AND del_flag = 0;
-- 应该返回 1
```

### 2. 应用层测试

#### 测试用例 1：新用户注册自动分配默认角色

```java
@Test
public void testNewUserGetDefaultRole() {
    // 创建新用户
    User user = createTestUser();
    
    // 触发注册事件
    RegisterEvent event = new RegisterEvent(user.getId());
    registerHandler.processRegisterEvent(event);
    
    // 验证用户已分配默认角色
    List<Role> userRoles = roleService.findRolesByIdUser(user.getId());
    assertFalse(userRoles.isEmpty());
    
    // 验证分配的是默认角色
    Role defaultRole = roleService.findDefaultRole();
    assertTrue(userRoles.stream().anyMatch(r -> r.getId().equals(defaultRole.getId())));
}
```

#### 测试用例 2：切换默认角色

```java
@Test
public void testSwitchDefaultRole() {
    // 获取当前默认角色
    Role currentDefault = roleService.findDefaultRole();
    
    // 创建新角色并设为默认
    Role newRole = new Role();
    newRole.setLabel("新默认角色");
    newRole.setPermission("new_default");
    newRole.setIsDefault(1);
    newRole.setStatus(0);
    roleService.saveRole(newRole);
    
    // 验证新角色成为默认角色
    Role newDefault = roleService.findDefaultRole();
    assertEquals(newRole.getId(), newDefault.getId());
    
    // 验证旧角色不再是默认
    Role oldRole = roleService.findById(currentDefault.getId());
    assertEquals(0, oldRole.getIsDefault().intValue());
}
```

#### 测试用例 3：无默认角色时的处理

```java
@Test
public void testNoDefaultRole() {
    // 将所有角色设为非默认
    // ... 设置逻辑
    
    // 创建新用户
    User user = createTestUser();
    
    // 触发注册事件（应该记录警告日志）
    RegisterEvent event = new RegisterEvent(user.getId());
    registerHandler.processRegisterEvent(event);
    
    // 验证用户没有被分配角色
    List<Role> userRoles = roleService.findRolesByIdUser(user.getId());
    assertTrue(userRoles.isEmpty());
}
```

### 3. 手动功能测试

1. **创建新用户，验证是否自动分配了默认角色**
   - 通过前端注册新用户
   - 查看用户详情，确认已分配角色
   
2. **修改默认角色，验证新注册用户是否使用了新的默认角色**
   - 在角色管理中，将某个角色设为默认
   - 注册新用户
   - 验证新用户获得的是新设置的默认角色

3. **删除所有默认角色标记，验证是否正确记录警告日志**
   - 将所有角色的 `is_default` 设为 0
   - 注册新用户
   - 查看日志，应该有警告信息

4. **并发测试（压力测试）**
   - 使用 JMeter 或类似工具并发注册多个用户
   - 验证所有用户都正确获得了默认角色
   - 验证系统中仍然只有一个默认角色

## 迁移日期

- 实施日期：待定
- 负责人：待定
- 版本：v1.x.x
