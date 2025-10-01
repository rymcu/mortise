# MenuService 参数绑定问题修复 - 测试指南

## 修复内容

已将 `MenuServiceImpl` 中的三个方法从使用原生 SQL 字符串改为使用 `@Select` 注解方法：

1. ✅ `findMenusByIdRole(Long idRole)` - 根据角色查询菜单
2. ✅ `findMenusByIdUser(Long idUser)` - 根据用户查询菜单
3. ✅ `findLinkTreeMode(Long idUser, long parentId)` - 递归查询用户菜单树

## 快速测试

### 1. 编译检查

```powershell
# 在项目根目录执行
mvn clean compile

# 或使用 Maven Wrapper
./mvnw clean compile
```

### 2. 单元测试（如果有）

```powershell
# 运行所有测试
mvn test

# 只运行 MenuService 相关测试
mvn test -Dtest=MenuServiceTest
```

### 3. 启动应用测试

```powershell
# 启动应用
mvn spring-boot:run

# 或
./mvnw spring-boot:run
```

### 4. API 测试

#### 测试场景 1: 获取用户菜单

```bash
# 前提：需要先登录获取 token
curl -X GET http://localhost:8080/api/v1/console/menus \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**预期结果**：
- ✅ 返回 200 状态码
- ✅ 返回用户的菜单树结构
- ✅ 没有参数绑定异常

#### 测试场景 2: 获取用户会话信息（包含菜单）

```bash
curl -X GET http://localhost:8080/api/v1/console/user/session \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**预期结果**：
- ✅ 返回用户信息、角色、权限和菜单
- ✅ 菜单数据正确

## SQL 日志验证

### 启用 MyBatis SQL 日志

在 `application.yml` 或 `application.properties` 中添加：

```yaml
# application.yml
mybatis-flex:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

# 或者
logging:
  level:
    com.rymcu.mortise.system.mapper: DEBUG
```

```properties
# application.properties
mybatis-flex.configuration.log-impl=org.apache.ibatis.logging.stdout.StdOutImpl

# 或者
logging.level.com.rymcu.mortise.system.mapper=DEBUG
```

### 预期日志输出

修复后，应该看到类似这样的日志：

```log
==>  Preparing: SELECT id, label, permission FROM mortise_menu tm WHERE del_flag = 0 AND EXISTS ( SELECT 1 FROM mortise_role_menu trm WHERE trm.id_mortise_menu = tm.id AND EXISTS ( SELECT 1 FROM mortise_user_role tur WHERE tur.id_mortise_role = trm.id_mortise_role AND tur.id_mortise_user = ? ) )
==> Parameters: 1(Long)
<==    Columns: id, label, permission
<==        Row: 1, 首页, dashboard
<==        Row: 2, 系统管理, system
<==      Total: 2
```

**关键点**：
- ✅ 参数个数正确（只有 1 个参数 - userId）
- ✅ 参数类型正确（Long）
- ✅ 没有 "栏位索引超过许可范围" 错误

## 常见错误排查

### 错误 1: 找不到 findMenusByIdUser 方法

**原因**：IDE 缓存问题

**解决方案**：
```powershell
# 清理并重新编译
mvn clean compile

# IntelliJ IDEA: File -> Invalidate Caches / Restart
```

### 错误 2: 参数绑定仍然失败

**检查点**：
1. 确认 `MenuMapper.java` 已正确导入 `@Select` 和 `@Param` 注解
2. 确认 SQL 中使用的是 `#{paramName}` 而不是 `{0}`
3. 检查 MyBatis-Flex 版本是否过旧

### 错误 3: 查询结果为空

**可能原因**：
1. 用户没有分配角色
2. 角色没有分配菜单权限
3. 菜单数据的 `del_flag` 不是 0

**排查 SQL**：
```sql
-- 检查用户角色关联
SELECT * FROM mortise_user_role WHERE id_mortise_user = ?;

-- 检查角色菜单关联
SELECT * FROM mortise_role_menu WHERE id_mortise_role = ?;

-- 检查菜单数据
SELECT * FROM mortise_menu WHERE del_flag = 0;
```

## 性能验证

### 使用 explain 分析查询

```sql
EXPLAIN ANALYZE
SELECT id, label, permission FROM mortise_menu tm 
WHERE del_flag = 0 
AND EXISTS (
  SELECT 1 FROM mortise_role_menu trm 
  WHERE trm.id_mortise_menu = tm.id 
  AND EXISTS (
    SELECT 1 FROM mortise_user_role tur 
    WHERE tur.id_mortise_role = trm.id_mortise_role 
    AND tur.id_mortise_user = 1
  )
);
```

### 性能建议

如果查询慢，建议添加以下索引：

```sql
-- mortise_role_menu 表
CREATE INDEX IF NOT EXISTS idx_role_menu_menu ON mortise_role_menu(id_mortise_menu);
CREATE INDEX IF NOT EXISTS idx_role_menu_role ON mortise_role_menu(id_mortise_role);

-- mortise_user_role 表
CREATE INDEX IF NOT EXISTS idx_user_role_user ON mortise_user_role(id_mortise_user);
CREATE INDEX IF NOT EXISTS idx_user_role_role ON mortise_user_role(id_mortise_role);

-- mortise_menu 表
CREATE INDEX IF NOT EXISTS idx_menu_parent ON mortise_menu(parent_id);
CREATE INDEX IF NOT EXISTS idx_menu_flag ON mortise_menu(del_flag);
```

## 回归测试清单

- [ ] 用户登录成功
- [ ] 获取用户菜单树（`/api/v1/console/menus`）
- [ ] 获取用户会话信息（`/api/v1/console/user/session`）
- [ ] 管理员可以看到所有菜单
- [ ] 普通用户只能看到授权的菜单
- [ ] 菜单树结构正确（父子关系）
- [ ] 菜单排序正确（按 sort_no）
- [ ] 没有参数绑定异常
- [ ] 日志中的 SQL 和参数正确

## 验证通过标准

✅ **修复成功的标志**：

1. 应用启动无错误
2. 调用菜单相关接口返回 200
3. 日志中没有 "栏位索引超过许可范围" 错误
4. 菜单数据正确返回
5. 参数绑定日志显示参数个数和类型正确

## 问题反馈

如果仍有问题，请提供以下信息：

1. 完整的错误堆栈
2. MyBatis 的 SQL 执行日志
3. MyBatis-Flex 版本号
4. PostgreSQL 版本号
5. 测试用的用户 ID 和角色信息

---

**最后更新**: 2025-10-02
**修复版本**: v1.0.0
