# DefaultFlag 枚举使用指南

## 概述

`DefaultFlag` 枚举用于替代硬编码的 0 和 1，表示是否为默认项（如默认角色、默认配置等）。

## 枚举定义

```java
package com.rymcu.mortise.common.enumerate;

public enum DefaultFlag {
    /**
     * 非默认
     */
    NO,      // ordinal = 0
    
    /**
     * 默认
     */
    YES;     // ordinal = 1
}
```

## 核心方法

### 1. `getValue()` - 获取整数值
```java
int value = DefaultFlag.YES.getValue();  // 返回 1
int value = DefaultFlag.NO.getValue();   // 返回 0
```

### 2. `valueOf(int)` - 根据整数获取枚举
```java
DefaultFlag flag = DefaultFlag.valueOf(1);  // 返回 DefaultFlag.YES
DefaultFlag flag = DefaultFlag.valueOf(0);  // 返回 DefaultFlag.NO
DefaultFlag flag = DefaultFlag.valueOf(2);  // 返回 null
```

### 3. `isDefault()` - 判断是否为默认
```java
boolean result = DefaultFlag.YES.isDefault();  // 返回 true
boolean result = DefaultFlag.NO.isDefault();   // 返回 false
```

## 使用场景

### 场景 1：实体类字段

```java
@Data
@Table(value = "mortise_role", schema = "mortise")
public class Role implements Serializable {
    /**
     * 是否为默认角色
     * 存储值：0-否，1-是
     */
    @Column(comment = "是否为默认角色")
    private Integer isDefault;
}
```

### 场景 2：保存时设置默认

```java
// ❌ 不推荐：硬编码
Role role = new Role();
role.setIsDefault(1);

// ✅ 推荐：使用枚举
Role role = new Role();
role.setIsDefault(DefaultFlag.YES.getValue());
```

### 场景 3：判断是否为默认

```java
// ❌ 不推荐：硬编码
if (role.getIsDefault() != null && role.getIsDefault() == 1) {
    // 处理默认角色
}

// ✅ 推荐：使用枚举
if (role.getIsDefault() != null && role.getIsDefault() == DefaultFlag.YES.getValue()) {
    // 处理默认角色
}

// ✅ 更推荐：使用枚举的工具方法
DefaultFlag flag = DefaultFlag.valueOf(role.getIsDefault());
if (flag != null && flag.isDefault()) {
    // 处理默认角色
}
```

### 场景 4：查询条件

```java
// ❌ 不推荐：硬编码
QueryWrapper queryWrapper = QueryWrapper.create()
    .where(ROLE.IS_DEFAULT.eq(1));

// ✅ 推荐：使用枚举
QueryWrapper queryWrapper = QueryWrapper.create()
    .where(ROLE.IS_DEFAULT.eq(DefaultFlag.YES.getValue()));
```

### 场景 5：更新操作

```java
// ❌ 不推荐：硬编码
Role updateRole = UpdateEntity.of(Role.class, roleId);
updateRole.setIsDefault(0);
mapper.update(updateRole);

// ✅ 推荐：使用枚举
Role updateRole = UpdateEntity.of(Role.class, roleId);
updateRole.setIsDefault(DefaultFlag.NO.getValue());
mapper.update(updateRole);
```

## 最佳实践

### ✅ DO（推荐做法）

1. **始终使用枚举值**
   ```java
   role.setIsDefault(DefaultFlag.YES.getValue());
   ```

2. **使用工具方法判断**
   ```java
   DefaultFlag flag = DefaultFlag.valueOf(role.getIsDefault());
   if (flag != null && flag.isDefault()) {
       // ...
   }
   ```

3. **在常量中使用枚举**
   ```java
   public static final int DEFAULT_ROLE = DefaultFlag.YES.getValue();
   ```

4. **添加清晰的注释**
   ```java
   // 设置为默认角色
   role.setIsDefault(DefaultFlag.YES.getValue());
   ```

### ❌ DON'T（不推荐做法）

1. **不要使用魔法数字**
   ```java
   // ❌ 不要这样
   role.setIsDefault(1);
   if (role.getIsDefault() == 1) { }
   ```

2. **不要直接比较字符串**
   ```java
   // ❌ 不要这样
   if ("1".equals(role.getIsDefault().toString())) { }
   ```

3. **不要忽略空值检查**
   ```java
   // ❌ 不要这样（可能 NPE）
   if (role.getIsDefault() == DefaultFlag.YES.getValue()) { }
   
   // ✅ 应该这样
   if (role.getIsDefault() != null && role.getIsDefault() == DefaultFlag.YES.getValue()) { }
   ```

## 与数据库映射

### 数据库字段定义

```sql
-- PostgreSQL
ALTER TABLE mortise.mortise_role 
ADD COLUMN is_default INTEGER DEFAULT 0;

-- MySQL
ALTER TABLE mortise.mortise_role 
ADD COLUMN is_default INT DEFAULT 0;
```

### 值映射关系

| 枚举 | Java值 | 数据库值 | 说明 |
|------|--------|----------|------|
| `DefaultFlag.NO` | `0` | `0` | 非默认 |
| `DefaultFlag.YES` | `1` | `1` | 默认 |

## 扩展使用

### 在 Service 层封装工具方法

```java
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {
    
    /**
     * 设置角色为默认角色
     */
    public void setAsDefault(Long roleId) {
        Role role = UpdateEntity.of(Role.class, roleId);
        role.setIsDefault(DefaultFlag.YES.getValue());
        mapper.update(role);
    }
    
    /**
     * 取消角色的默认状态
     */
    public void unsetDefault(Long roleId) {
        Role role = UpdateEntity.of(Role.class, roleId);
        role.setIsDefault(DefaultFlag.NO.getValue());
        mapper.update(role);
    }
    
    /**
     * 判断角色是否为默认角色
     */
    public boolean isDefaultRole(Role role) {
        if (role.getIsDefault() == null) {
            return false;
        }
        DefaultFlag flag = DefaultFlag.valueOf(role.getIsDefault());
        return flag != null && flag.isDefault();
    }
}
```

### 在 Controller 层使用

```java
@RestController
@RequestMapping("/api/roles")
public class RoleController {
    
    @PostMapping("/setDefault/{id}")
    public Result setDefaultRole(@PathVariable Long id) {
        Role role = roleService.findById(id);
        role.setIsDefault(DefaultFlag.YES.getValue());
        roleService.saveRole(role);
        return Result.success();
    }
}
```

## 其他枚举参考

系统中还有其他类似的枚举可供参考：

### Status 枚举（状态）
```java
public enum Status {
    DISABLED,  // 禁用 = 0
    ENABLED    // 启用 = 1
}
```

### DelFlag 枚举（删除标记）
```java
public enum DelFlag {
    NORMAL,    // 正常 = 0
    DELETED    // 已删除 = 1
}
```

## 迁移指南

### 从硬编码迁移到枚举

1. **查找所有硬编码**
   ```bash
   # 在项目中搜索
   grep -r "setIsDefault(0)" .
   grep -r "setIsDefault(1)" .
   grep -r ".eq(1)" . | grep "IS_DEFAULT"
   ```

2. **替换为枚举**
   - `setIsDefault(0)` → `setIsDefault(DefaultFlag.NO.getValue())`
   - `setIsDefault(1)` → `setIsDefault(DefaultFlag.YES.getValue())`
   - `IS_DEFAULT.eq(1)` → `IS_DEFAULT.eq(DefaultFlag.YES.getValue())`

3. **添加导入语句**
   ```java
   import com.rymcu.mortise.common.enumerate.DefaultFlag;
   ```

## 单元测试

```java
@Test
public void testDefaultFlag() {
    // 测试值映射
    assertEquals(0, DefaultFlag.NO.getValue());
    assertEquals(1, DefaultFlag.YES.getValue());
    
    // 测试 valueOf
    assertEquals(DefaultFlag.NO, DefaultFlag.valueOf(0));
    assertEquals(DefaultFlag.YES, DefaultFlag.valueOf(1));
    assertNull(DefaultFlag.valueOf(2));
    
    // 测试 isDefault
    assertTrue(DefaultFlag.YES.isDefault());
    assertFalse(DefaultFlag.NO.isDefault());
    
    // 测试在实体中使用
    Role role = new Role();
    role.setIsDefault(DefaultFlag.YES.getValue());
    assertEquals(1, role.getIsDefault().intValue());
}
```

## 常见问题

### Q1: 为什么不直接使用布尔类型？

**A:** 
- 历史原因，数据库中已使用 `0/1` 表示
- 整数类型更灵活，未来可能扩展更多状态
- 保持与其他枚举（Status、DelFlag）的一致性

### Q2: 枚举的 ordinal() 值是否稳定？

**A:** 
- 只要不改变枚举定义的顺序，ordinal() 值就是稳定的
- `NO` 始终是 0（第一个），`YES` 始终是 1（第二个）
- 不要在枚举中间插入新值

### Q3: 如何处理遗留代码？

**A:**
- 渐进式重构，优先处理新功能
- 在代码审查时要求使用枚举
- 可以编写 IDE 插件或脚本批量替换

## 总结

使用 `DefaultFlag` 枚举的好处：

1. ✅ **消除魔法数字**：代码更易读
2. ✅ **类型安全**：编译时检查
3. ✅ **易于维护**：统一管理常量
4. ✅ **自文档化**：枚举名即文档
5. ✅ **IDE 支持**：自动补全、重构方便

**记住：始终使用枚举，拒绝硬编码！** 🚀
