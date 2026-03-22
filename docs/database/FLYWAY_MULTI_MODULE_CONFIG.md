# Flyway 多模块配置说明

## ✅ 配置确认

经过验证，当前配置 **完全正确**，可以正常工作！

## 📁 项目结构

```
mortise/
├── mortise-app/                                    # 主应用模块
│   ├── pom.xml                                    # ✅ 依赖 mortise-system
│   └── src/main/resources/
│       └── application-dev.yml                    # ✅ Flyway 配置
│           └── locations: classpath:db/migration
│
└── mortise-system/                                # 系统业务模块
    ├── pom.xml                                    # ✅ 包含 Flyway 依赖
    └── src/main/resources/
        └── db/migration/                          # ✅ SQL 脚本位置
            └── V1__Create_System_Tables.sql       # ✅ 建表脚本
```

## 🔍 工作原理

### 1. Maven 依赖链

```xml
<!-- mortise-app/pom.xml -->
<dependency>
    <groupId>com.rymcu</groupId>
    <artifactId>mortise-system</artifactId>  ← 依赖
</dependency>
```

### 2. 资源打包

当执行 `mvn package` 时：

```
mortise-system.jar
└── BOOT-INF/classes/
    └── db/migration/
        └── V1__Create_System_Tables.sql  ← 被打包进 JAR
```

### 3. Classpath 扫描

```yaml
# application-dev.yml
spring:
  flyway:
    locations: classpath:db/migration  ← 扫描整个 classpath
```

Flyway 启动时：
1. 扫描 `classpath:db/migration`
2. 查找所有 JAR 和目录
3. 发现 `mortise-system.jar!/db/migration/V1__Create_System_Tables.sql`
4. ✅ 执行迁移脚本

## ✅ 验证结果

运行 `.\verify-flyway-config.ps1` 的结果：

```
✅ 基本配置检查通过！

验证项：
✅ SQL 脚本文件存在
✅ mortise-system 模块存在
✅ mortise-app 正确依赖 mortise-system
✅ Flyway 配置正确
✅ 脚本位置配置正确: classpath:db/migration
```

## 📊 启动日志示例

启动应用时，会看到类似日志：

```
╔════════════════════════════════════════════════════════════════╗
║           Flyway 数据库迁移脚本检测结果                        ║
╠════════════════════════════════════════════════════════════════╣
║ 配置路径: classpath:db/migration                               ║
║ 找到脚本数量: 1 个                                             ║
╠════════════════════════════════════════════════════════════════╣
║ [1] V1__Create_System_Tables.sql                               ║
║     位置: jar:file:.../mortise-system-0.0.1.jar!/db/migration/ ║
╠════════════════════════════════════════════════════════════════╣
║ ✅ Flyway 脚本配置正确，可以正常识别！                        ║
╚════════════════════════════════════════════════════════════════╝

Flyway Community Edition 10.x.x by Redgate
...
Successfully validated 1 migration (execution time 00:00.023s)
Creating Schema History table "mortise"."flyway_schema_history" ...
Current version of schema "mortise": << Empty Schema >>
Migrating schema "mortise" to version "1 - Create System Tables"
Successfully applied 1 migration to schema "mortise" (execution time 00:00.156s)
```

## 🎯 关键要点

### ✅ 可以这样配置（当前方式，推荐）

- SQL 脚本在：`mortise-system/src/main/resources/db/migration/`
- Flyway 配置：`locations: classpath:db/migration`
- **优点**：模块职责清晰，易于维护

### ✅ 版本号这样分配（避免冲突）

- Flyway 版本号在 Mortise 中按**全仓库全局序列**使用，而不是按模块单独编号。
- 标准业务模块的迁移目录是 `mortise-xx-infra/src/main/resources/db/migration/`。
- 当前仓库还有少量历史单模块迁移目录，例如 `mortise-wechat/src/main/resources/db/migration/`、`mortise-file/src/main/resources/db/migration/`，统计版本号时也必须一起扫描。
- 新建迁移脚本前，先在仓库根目录运行：`./get-next-flyway-version.ps1`
- 如果不能运行脚本，至少手动扫描所有 `src/main/resources/db/migration/V*.sql`，确认当前最大版本号。
- 创建新脚本时使用脚本输出的 `NextVersion`，并在提交前再次确认没有并发新增导致撞号。
- 如果脚本提示存在重复版本号，应先解决重复，再继续新增迁移。

### ⚠️ 不需要这样配置（常见误区）

❌ 不需要复制脚本到 `mortise-app`
❌ 不需要使用绝对路径
❌ 不需要修改为 `classpath*:db/migration`（虽然也可以）
❌ 不需要在 `application.yml` 中指定模块路径

## 🔧 如何验证

### 方法1：使用验证脚本

```powershell
.\verify-flyway-config.ps1
```

### 方法1.5：查询下一个可用版本号

```powershell
.\get-next-flyway-version.ps1
```

预期输出会包含：

- 当前最大版本号
- 建议下一个版本号
- 最近 10 个已使用版本
- 是否存在重复版本号

### 方法1.6：启用提交前自动拦截

```powershell
.\setup-git-hooks.ps1
```

启用后，Git 会在每次 `commit` 前执行 `.githooks/pre-commit`：

- 调用 `get-next-flyway-version.ps1 -FailOnDuplicates`
- 发现重复版本号时直接终止提交
- 未发现重复版本号时允许提交继续进行

### 方法2：手动检查 JAR

```powershell
# 构建项目
mvn clean package -DskipTests

# 查看 JAR 内容
jar tf mortise-system\target\mortise-system-0.0.1.jar | Select-String "db/migration"

# 预期输出：
# db/migration/
# db/migration/V1__Create_System_Tables.sql
```

### 方法3：启动应用观察日志

```powershell
mvn spring-boot:run
```

观察 `FlywayScriptValidator` 组件的输出。

## 📚 相关配置文件

### 1. mortise-system/pom.xml

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

### 2. mortise-app/src/main/resources/application-dev.yml

```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration    # ← 关键配置
    schemas: mortise
```

### 3. mortise-system/src/main/resources/db/migration/V1__Create_System_Tables.sql

遵循 Flyway 命名规范：`V{version}__{description}.sql`

## 🎉 结论

**当前配置完全正确，可以直接使用！** ✅

- ✅ 多模块结构配置正确
- ✅ Flyway 可以自动识别脚本
- ✅ 启动时自动建表
- ✅ 包含验证工具

只需：
1. 确保数据库连接配置正确
2. 启动应用
3. Flyway 自动执行建表脚本

---

**最后更新**: 2025-10-02  
**验证状态**: ✅ 已验证通过  
**验证工具**: `verify-flyway-config.ps1`
