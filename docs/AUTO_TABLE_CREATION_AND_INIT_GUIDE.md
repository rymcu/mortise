# 系统自动建表与初始化指南

## 📋 概述

本项目使用 **Flyway** 实现数据库自动建表功能，并通过**系统初始化引导程序**实现首次启动时的数据初始化。

## 🚦 快速验证

在开始之前，可以运行配置验证脚本确认一切就绪：

```powershell
# Windows PowerShell
.\verify-flyway-config.ps1
```

该脚本会自动检查：
- ✅ SQL 脚本文件是否存在
- ✅ 模块依赖是否正确配置
- ✅ Flyway 配置是否正确
- ✅ 多模块结构是否符合要求

**预期输出：**
```
╔════════════════════════════════════════════════════════════════╗
║  验证结果                                                      ║
╚════════════════════════════════════════════════════════════════╝

✅ 基本配置检查通过！
```

## 🚀 自动建表

### 配置说明

项目已配置 Flyway 数据库迁移工具，首次启动时会自动执行以下操作：

1. **自动创建 Schema**: `mortise`
2. **自动创建数据表**:
   - `mortise_user` - 用户表
   - `mortise_role` - 角色表
   - `mortise_menu` - 菜单表
   - `mortise_user_role` - 用户角色关联表
   - `mortise_role_menu` - 角色菜单关联表
   - `mortise_dict_type` - 字典类型表
   - `mortise_dict` - 字典数据表

### Flyway 配置

在 `application-dev.yml` 中的配置：

```yaml
spring:
  flyway:
    enabled: true                        # 启用 Flyway
    baseline-on-migrate: true            # 如果数据库不是空的，首次迁移时自动创建基线
    baseline-version: 0                  # 基线版本号
    locations: classpath:db/migration    # SQL脚本位置
    encoding: UTF-8                      # SQL脚本编码
    validate-on-migrate: true            # 迁移前验证SQL脚本
    out-of-order: false                  # 禁止乱序迁移
    clean-disabled: true                 # 禁用clean命令（生产环境必须禁用）
    schemas: mortise                     # 指定schema
    table: flyway_schema_history         # 迁移历史记录表名
```

### SQL 脚本位置

建表脚本位于：`mortise-system/src/main/resources/db/migration/V1__Create_System_Tables.sql`

**📌 重要说明：多模块项目配置**

本项目是 Maven 多模块结构，Flyway 配置在 `mortise-app` 模块中，SQL 脚本在 `mortise-system` 模块中：

```
mortise/
├── mortise-app/                              # 主应用模块
│   └── src/main/resources/
│       └── application-dev.yml              # Flyway 配置在这里
├── mortise-system/                          # 系统业务模块
│   └── src/main/resources/
│       └── db/migration/                    # SQL 脚本在这里
│           └── V1__Create_System_Tables.sql
```

**✅ 配置可以正常工作，原因：**

1. `mortise-app` 依赖了 `mortise-system` 模块（在 pom.xml 中声明）
2. 打包时，`mortise-system` 的资源文件会包含在 classpath 中
3. Flyway 的 `classpath:db/migration` 会扫描整个 classpath，包括依赖模块
4. 因此可以正确找到 `mortise-system` 中的 SQL 脚本

**🔍 验证方法：**

项目已包含 `FlywayScriptValidator` 组件，启动时会自动验证并输出日志：

```
╔════════════════════════════════════════════════════════════════╗
║           Flyway 数据库迁移脚本检测结果                        ║
╠════════════════════════════════════════════════════════════════╣
║ 配置路径: classpath:db/migration                               ║
║ 找到脚本数量: 1 个                                             ║
╠════════════════════════════════════════════════════════════════╣
║ [1] V1__Create_System_Tables.sql                               ║
║     位置: jar:file:/path/to/mortise-system.jar!/db/migration/  ║
╠════════════════════════════════════════════════════════════════╣
║ ✅ Flyway 脚本配置正确，可以正常识别！                        ║
╚════════════════════════════════════════════════════════════════╝
```

如果未找到脚本，日志会提示错误信息和排查步骤。

### 脚本命名规范

Flyway 要求脚本遵循以下命名规范：

```
V{version}__{description}.sql
```

例如：
- `V1__Create_System_Tables.sql` - 创建系统表
- `V2__Add_User_Avatar_Column.sql` - 添加用户头像字段（示例）

## 🎯 系统初始化引导

### 功能说明

首次启动系统时，可以通过引导程序完成以下初始化：

1. ✅ 初始化字典类型和字典数据
   - 状态字典（正常/禁用）
   - 删除标记字典（未删除/已删除）
   - 菜单类型字典（目录/菜单/按钮）

2. ✅ 初始化角色
   - 超级管理员角色
   - 普通用户角色（默认）

3. ✅ 初始化菜单
   - 系统管理模块
   - 用户管理、角色管理、菜单管理、字典管理

4. ✅ 创建管理员账号
   - 可自定义管理员账号、密码、昵称、邮箱

5. ✅ 分配管理员权限

### API 接口

#### 1. 检查系统初始化状态

```http
GET /api/v1/system-init/status
```

**响应示例：**
```json
{
  "initialized": false
}
```

#### 2. 执行系统初始化

```http
POST /api/v1/system-init/initialize
Content-Type: application/json

{
  "adminAccount": "admin",
  "adminPassword": "admin123",
  "adminNickname": "超级管理员",
  "adminEmail": "admin@mortise.com",
  "systemName": "Mortise",
  "systemDescription": "现代化后台管理系统"
}
```

**响应示例：**
```json
{
  "success": true,
  "message": "系统初始化成功"
}
```

#### 3. 获取初始化进度

```http
GET /api/v1/system-init/progress
```

**响应示例：**
```json
{
  "progress": 60
}
```

### 前端集成示例

```typescript
// 1. 检查系统是否需要初始化
async function checkInitStatus() {
  const response = await fetch('/api/v1/system-init/status');
  const data = await response.json();
  
  if (!data.initialized) {
    // 跳转到初始化引导页面
    router.push('/init-wizard');
  }
}

// 2. 执行系统初始化
async function initializeSystem(initInfo) {
  const response = await fetch('/api/v1/system-init/initialize', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(initInfo)
  });
  
  const result = await response.json();
  if (result.success) {
    // 初始化成功，跳转到登录页
    router.push('/login');
  }
}

// 3. 轮询获取初始化进度
async function pollProgress() {
  const interval = setInterval(async () => {
    const response = await fetch('/api/v1/system-init/progress');
    const data = await response.json();
    
    updateProgressBar(data.progress);
    
    if (data.progress >= 100) {
      clearInterval(interval);
    }
  }, 500);
}
```

## 📦 Maven 依赖

项目已添加 Flyway 依赖（在 `mortise-system/pom.xml`）：

```xml
<!-- Flyway for Database Migration -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

## 🔧 主键生成策略

本项目使用 MyBatis-Flex 的 `flexId` 生成分布式 ID：

```java
@Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
private Long id;
```

**特点：**
- 由应用层生成，无需数据库自增序列
- 支持分布式环境
- 64位长整型，性能优异

## ⚙️ 启动流程

### 首次启动

1. **启动应用**
   ```bash
   mvn spring-boot:run
   ```

2. **Flyway 自动执行**
   - 检查数据库连接
   - 创建 `flyway_schema_history` 表
   - 执行 `V1__Create_System_Tables.sql` 脚本
   - 记录迁移历史

3. **访问系统**
   - 系统检测到未初始化状态
   - 前端引导用户进入初始化页面

4. **完成初始化**
   - 用户填写管理员信息
   - 调用初始化接口
   - 系统创建基础数据
   - 跳转到登录页面

### 后续启动

- Flyway 检测到已执行过的脚本，跳过执行
- 系统检测到已初始化，正常启动

## 📝 添加新的数据库迁移

当需要修改数据库结构时：

1. 在 `mortise-system/src/main/resources/db/migration/` 目录下创建新的 SQL 脚本
2. 命名遵循规范：`V{version}__{description}.sql`
   - 版本号必须大于已有脚本
   - 例如：`V2__Add_User_Phone_Column.sql`

3. 编写 SQL 脚本：
   ```sql
   -- V2__Add_User_Phone_Column.sql
   ALTER TABLE mortise.mortise_user ADD COLUMN phone VARCHAR(20);
   ```

4. 重启应用，Flyway 自动执行新脚本

## 🎨 初始化引导页面示例

建议在前端实现一个多步骤的初始化向导：

### Step 1: 欢迎页
- 显示系统介绍
- 提示用户需要初始化

### Step 2: 管理员账号配置
- 账号（必填）
- 密码（必填，强度校验）
- 昵称（必填）
- 邮箱（必填，格式校验）

### Step 3: 系统配置
- 系统名称
- 系统描述

### Step 4: 初始化进度
- 显示进度条
- 实时更新进度

### Step 5: 完成
- 显示初始化成功信息
- 提供登录按钮

## 🔒 安全建议

1. **生产环境配置**：
   ```yaml
   spring:
     flyway:
       clean-disabled: true  # 必须禁用，防止误删数据
   ```

2. **初始化接口保护**：
   - 添加访问令牌验证
   - 或在初始化完成后禁用接口
   - 建议添加 IP 白名单

3. **管理员密码**：
   - 前端强制要求强密码
   - 后端使用 BCrypt 加密存储

## 📊 迁移历史查询

Flyway 会自动创建 `flyway_schema_history` 表记录迁移历史：

```sql
SELECT * FROM mortise.flyway_schema_history ORDER BY installed_rank;
```

## 🧪 手动验证配置

### 方法1：检查打包后的 JAR

```bash
# 1. 构建项目
mvn clean package -DskipTests

# 2. 查看 mortise-system JAR 内容
jar tf mortise-system/target/mortise-system-0.0.1.jar | grep "db/migration"

# 预期输出：
# db/migration/
# db/migration/V1__Create_System_Tables.sql
```

### 方法2：运行时验证

启动应用后，查看日志输出，应该能看到 `FlywayScriptValidator` 的验证信息。

### 方法3：使用 Maven 插件

也可以在 `mortise-app/pom.xml` 中直接配置 Flyway Maven 插件进行验证：

```xml
<plugin>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-maven-plugin</artifactId>
    <version>${flyway.version}</version>
    <configuration>
        <url>jdbc:postgresql://localhost:5432/postgres</url>
        <user>mortise</user>
        <password>your_password</password>
        <schemas>
            <schema>mortise</schema>
        </schemas>
        <locations>
            <location>classpath:db/migration</location>
        </locations>
    </configuration>
</plugin>
```

然后运行：
```bash
mvn flyway:info
```

## 🛠️ 故障排除

### 问题1：权限拒绝错误（最常见）

**错误信息：**
```
ERROR: permission denied for schema mortise
SQL State: 42501
```

**原因：** 数据库用户没有在 schema 上的操作权限

**快速解决：**

```powershell
# 运行权限修复脚本
.\fix-postgresql-permissions.ps1
```

**手动解决：**

以超级用户身份（如 `postgres`）执行：

```sql
-- 授予数据库级别权限（关键！）
GRANT CREATE ON DATABASE postgres TO mortise;
GRANT CONNECT ON DATABASE postgres TO mortise;

-- 创建 schema
CREATE SCHEMA IF NOT EXISTS mortise;

-- 设置 schema 所有者
ALTER SCHEMA mortise OWNER TO mortise;

-- 授予必要权限
GRANT USAGE ON SCHEMA mortise TO mortise;
GRANT CREATE ON SCHEMA mortise TO mortise;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA mortise TO mortise;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA mortise TO mortise;

-- 设置默认权限
ALTER DEFAULT PRIVILEGES IN SCHEMA mortise 
    GRANT ALL PRIVILEGES ON TABLES TO mortise;
```

**详细说明：** 参见 [FLYWAY_PERMISSION_FIX.md](./FLYWAY_PERMISSION_FIX.md)

### 问题2：Flyway 迁移失败

**检查项：**
- 数据库连接是否正常
- SQL 脚本语法是否正确
- 用户权限是否足够（参见问题1）

**解决方法：**
```sql
-- 查看失败记录
SELECT * FROM mortise.flyway_schema_history WHERE success = false;

-- 手动修复后，更新状态
UPDATE mortise.flyway_schema_history 
SET success = true 
WHERE version = '1';
```

### 问题2：找不到 Flyway 迁移脚本

**现象：** 启动时提示找不到 SQL 脚本

**可能原因：**
1. `mortise-system` 模块未正确构建
2. `mortise-app` 未正确依赖 `mortise-system`
3. SQL 脚本文件名不符合规范

**解决方法：**

```bash
# 1. 重新构建 mortise-system
cd mortise-system
mvn clean install

# 2. 检查 JAR 包内容
jar tf target/mortise-system-0.0.1.jar | grep "db/migration"

# 3. 确认 mortise-app 的依赖
cd ../mortise-app
mvn dependency:tree | grep mortise-system
```

**验证脚本位置：**

```bash
# 在项目根目录执行
find . -name "V1__Create_System_Tables.sql"

# 应该输出：
# ./mortise-system/src/main/resources/db/migration/V1__Create_System_Tables.sql
```

### 问题3：重复初始化

系统会自动检测是否已初始化，不允许重复初始化。如需重新初始化：

```sql
-- 清空所有表数据（谨慎操作！）
TRUNCATE TABLE mortise.mortise_user CASCADE;
TRUNCATE TABLE mortise.mortise_role CASCADE;
-- ... 其他表
```

### 问题4：多模块项目 Flyway 配置建议

**推荐配置方式（当前使用）：**

✅ SQL 脚本在业务模块 (`mortise-system`)
✅ Flyway 配置在应用模块 (`mortise-app`)
✅ 使用 `classpath:db/migration`（会自动扫描依赖模块）

**优点：**
- 业务模块包含自己的数据库脚本，职责清晰
- 支持多个业务模块各自管理数据库脚本
- 易于模块化开发和维护

**替代方案（如果遇到问题）：**

可以将脚本复制到 `mortise-app/src/main/resources/db/migration/`：

```bash
# Windows PowerShell
Copy-Item -Path "mortise-system/src/main/resources/db/migration/*" `
          -Destination "mortise-app/src/main/resources/db/migration/" `
          -Recurse -Force
```

但这会导致维护问题，不推荐！

## 📖 相关文档

- [Flyway 官方文档](https://flywaydb.org/documentation/)
- [MyBatis-Flex 官方文档](https://mybatis-flex.com/)
- [PostgreSQL 官方文档](https://www.postgresql.org/docs/)

## ✨ 总结

✅ **自动建表**：使用 Flyway，首次启动自动创建所有表结构  
✅ **数据初始化**：通过引导程序，用户自定义初始化基础数据  
✅ **版本管理**：Flyway 记录所有迁移历史，支持版本回滚  
✅ **安全可靠**：事务支持，失败自动回滚  
✅ **易于维护**：SQL 脚本化管理，清晰明了

---

**作者**: ronger  
**日期**: 2025-10-02  
**版本**: v1.0
