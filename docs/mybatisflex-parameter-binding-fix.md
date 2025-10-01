# MyBatis-Flex 参数绑定问题修复

## 问题描述

在使用 MyBatis-Flex 的 `QueryWrapper` 时，遇到以下错误：

```
org.springframework.dao.DataIntegrityViolationException: Error setting non null for parameter #3 with JdbcType null.
Try setting a different JdbcType for this parameter or a different configuration property.
Cause: org.postgresql.util.PSQLException: 栏位索引超过许可范围：3，栏位数：2。
```

## 问题原因

在 `MenuServiceImpl` 中的三个方法使用了原生 SQL 字符串和占位符 `{0}`：

```java
// ❌ 问题代码
QueryWrapper queryWrapper = QueryWrapper.create()
    .select(MENU.ID, MENU.LABEL, MENU.PERMISSION)
    .where(MENU.DEL_FLAG.eq(0))
    .and("EXISTS (SELECT 1 FROM mortise_role_menu trm WHERE trm.id_mortise_menu = mortise_menu.id " +
         "AND EXISTS (SELECT 1 FROM mortise_user_role tur WHERE tur.id_mortise_role = trm.id_mortise_role AND tur.id_mortise_user = {0}))", idUser);
```

**核心问题**：
1. MyBatis-Flex 在处理复杂嵌套的 EXISTS 子查询时，占位符 `{0}` 的参数绑定可能出现错误
2. 参数索引计算错误，导致 "栏位索引超过许可范围：3，栏位数：2"
3. 在 PostgreSQL 环境下这个问题更容易暴露

## 解决方案

将原生 SQL 字符串查询改为使用 MyBatis 的 `@Select` 注解方法，让 MyBatis 直接处理参数绑定。

### 步骤 1: 在 MenuMapper 中添加方法

```java
@Mapper
public interface MenuMapper extends BaseMapper<Menu> {

    /**
     * 根据角色ID查询菜单列表
     */
    @Select("SELECT id, label, permission FROM mortise_menu tm " +
            "WHERE del_flag = 0 " +
            "AND EXISTS (" +
            "  SELECT 1 FROM mortise_role_menu trm " +
            "  WHERE trm.id_mortise_menu = tm.id " +
            "  AND trm.id_mortise_role = #{idRole}" +
            ")")
    List<Menu> findMenusByIdRole(@Param("idRole") Long idRole);

    /**
     * 根据用户ID查询菜单列表（通过用户角色关联）
     */
    @Select("SELECT id, label, permission FROM mortise_menu tm " +
            "WHERE del_flag = 0 " +
            "AND EXISTS (" +
            "  SELECT 1 FROM mortise_role_menu trm " +
            "  WHERE trm.id_mortise_menu = tm.id " +
            "  AND EXISTS (" +
            "    SELECT 1 FROM mortise_user_role tur " +
            "    WHERE tur.id_mortise_role = trm.id_mortise_role " +
            "    AND tur.id_mortise_user = #{idUser}" +
            "  )" +
            ")")
    List<Menu> findMenusByIdUser(@Param("idUser") Long idUser);

    /**
     * 根据用户ID和父菜单ID查询菜单链接（树形结构）
     */
    @Select("SELECT id, label, permission, parent_id, sort_no, menu_type, icon, href " +
            "FROM mortise_menu tm " +
            "WHERE del_flag = 0 " +
            "AND menu_type = 0 " +
            "AND parent_id = #{parentId} " +
            "AND EXISTS (" +
            "  SELECT 1 FROM mortise_role_menu trm " +
            "  WHERE trm.id_mortise_menu = tm.id " +
            "  AND EXISTS (" +
            "    SELECT 1 FROM mortise_user_role tur " +
            "    WHERE tur.id_mortise_role = trm.id_mortise_role " +
            "    AND tur.id_mortise_user = #{idUser}" +
            "  )" +
            ") " +
            "ORDER BY sort_no ASC")
    List<Menu> findLinksByUserIdAndParentId(@Param("idUser") Long idUser, @Param("parentId") Long parentId);
}
```

### 步骤 2: 简化 Service 实现

```java
@Override
public List<Menu> findMenusByIdRole(Long idRole) {
    // ✅ 直接调用 Mapper 方法
    return mapper.findMenusByIdRole(idRole);
}

@Override
public List<Menu> findMenusByIdUser(Long idUser) {
    // ✅ 直接调用 Mapper 方法
    return mapper.findMenusByIdUser(idUser);
}

private List<Link> findLinkTreeMode(Long idUser, long parentId) {
    // ✅ 直接调用 Mapper 方法
    List<Menu> menus = mapper.findLinksByUserIdAndParentId(idUser, parentId);
    List<Link> links = new ArrayList<>();
    for (Menu menu : menus) {
        Link link = convertLink(menu);
        link.setChildren(findLinkTreeMode(idUser, menu.getId()));
        links.add(link);
    }
    return links;
}
```

## 对比分析

| 对比项 | 原生 SQL 字符串 (QueryWrapper) | @Select 注解方法 |
|--------|--------------------------------|------------------|
| 参数绑定 | 使用 `{0}` 占位符，容易出错 | 使用 `#{param}` 标准方式，稳定可靠 |
| 调试难度 | SQL 拼接复杂，难以调试 | SQL 清晰，易于调试 |
| 代码可读性 | 代码冗长，逻辑混杂 | 代码简洁，职责清晰 |
| 性能 | 相同 | 相同 |
| 数据库兼容性 | 在 PostgreSQL 下容易出问题 | 兼容性好 |

## 最佳实践建议

### 1. **何时使用 QueryWrapper**
适用于简单的动态查询：
```java
QueryWrapper queryWrapper = QueryWrapper.create()
    .select()
    .where(MENU.LABEL.like(label, StringUtils.isNotBlank(label)))
    .and(MENU.STATUS.eq(status, Objects.nonNull(status)))
    .orderBy(MENU.SORT_NO.asc());
```

### 2. **何时使用 @Select 注解**
适用于以下场景：
- ✅ 复杂的子查询（特别是嵌套的 EXISTS）
- ✅ 需要使用原生 SQL 函数
- ✅ 固定的查询逻辑（不需要动态条件）
- ✅ 跨表复杂关联查询

### 3. **何时使用 XML Mapper**
适用于以下场景：
- ✅ 非常复杂的 SQL（超过 20 行）
- ✅ 需要复杂的动态 SQL 逻辑
- ✅ 需要结果映射（ResultMap）
- ✅ 团队更习惯 XML 方式

## 参数绑定规则

### MyBatis-Flex QueryWrapper 占位符

```java
// 正确方式：简单条件使用 QueryColumn
.where(MENU.ID.eq(menuId))
.and(MENU.STATUS.eq(status))

// 谨慎使用：原生 SQL 字符串 + 占位符
.and("column = {0}", value)  // 简单场景可用
.and("EXISTS (SELECT 1 FROM table WHERE id = {0})", id)  // 可能有问题

// ❌ 避免：复杂嵌套的原生 SQL
.and("EXISTS (SELECT 1 FROM t1 WHERE EXISTS (SELECT 1 FROM t2 WHERE id = {0}))", id)
```

### MyBatis 标准占位符

```java
// @Select 注解中使用
@Select("SELECT * FROM menu WHERE id = #{id}")
Menu findById(@Param("id") Long id);

// 多参数
@Select("SELECT * FROM menu WHERE id = #{id} AND status = #{status}")
Menu findByIdAndStatus(@Param("id") Long id, @Param("status") Integer status);
```

## 修复验证

修复后，以下操作应该正常工作：

1. **根据用户ID获取菜单权限**
```java
List<Menu> menus = menuService.findMenusByIdUser(userId);
```

2. **根据角色ID获取菜单权限**
```java
List<Menu> menus = menuService.findMenusByIdRole(roleId);
```

3. **获取用户的菜单树**
```java
List<Link> menuTree = menuService.findLinksByIdUser(userId);
```

## 注意事项

1. **PostgreSQL 参数索引**：PostgreSQL 对参数索引的校验更严格，容易暴露参数绑定问题
2. **MyBatis-Flex 版本**：确保使用最新版本的 MyBatis-Flex，老版本可能有更多 bug
3. **日志调试**：启用 MyBatis 的 SQL 日志可以看到实际执行的 SQL 和参数绑定情况

```yaml
# application.yml
mybatis-flex:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

## 总结

通过将复杂的原生 SQL 字符串查询改为 `@Select` 注解方法：
- ✅ 解决了参数绑定错误问题
- ✅ 提高了代码可读性
- ✅ 便于维护和调试
- ✅ 提升了数据库兼容性

这是 MyBatis-Flex 官方推荐的最佳实践！🎯
