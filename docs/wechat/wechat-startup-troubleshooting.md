# 微信模块启动检查指南

## 问题诊断：为什么启动时看不到 WeChat 模块日志？

### 根本原因
1. **缺少模块依赖**: `mortise-app` 的 `pom.xml` 中没有引入 `mortise-wechat` 依赖
2. **缺少配置**: `application-dev.yml` 中没有 WeChat 相关配置
3. **数据库表缺失**: 没有执行 WeChat 模块的数据库迁移脚本

### 已修复的问题 ✅

#### 1. 添加模块依赖
在 `mortise-app/pom.xml` 中已添加：
```xml
<dependency>
    <groupId>com.rymcu</groupId>
    <artifactId>mortise-wechat</artifactId>
</dependency>
```

#### 2. 添加配置文件
在 `application-dev.yml` 中已添加：
```yaml
# 微信配置
wechat:
  # 微信公众号配置
  mp:
    enabled: true
  # 微信开放平台配置  
  open:
    enabled: true
```

> **注意**: Jasypt 加密配置已在 `mortise-core` 模块中配置完成，无需重复配置。

#### 3. 添加数据库迁移脚本
创建了 `V2__Create_WeChat_Multi_Account_Tables.sql`，包含：
- `mortise_wechat_account` 表（账号管理）
- `mortise_wechat_config` 表（配置详情）
- 示例数据（默认禁用状态）

## 启动后的预期日志输出

### 1. 模块加载日志
```
[main] INFO  com.rymcu.mortise.wechat.config.WeChatMpConfiguration - 微信多账号配置服务未启用，跳过默认公众号服务初始化
[main] INFO  com.rymcu.mortise.wechat.config.WeChatOpenConfiguration - 微信多账号配置服务未启用，跳过默认开放平台服务初始化
```

### 2. 如果配置了有效账号，会看到：
```
[main] INFO  com.rymcu.mortise.wechat.config.WeChatMpConfiguration - 微信公众号默认服务初始化成功（数据库配置），AppID: wx1***abc
[main] INFO  com.rymcu.mortise.wechat.config.WeChatMpConfiguration - 微信公众号多账号服务初始化完成，共 2 个账号
[main] INFO  com.rymcu.mortise.wechat.config.WeChatOpenConfiguration - 微信开放平台默认服务初始化成功（数据库配置），AppID: wx2***def
```

### 3. 数据库迁移日志
```
[main] INFO  org.flywaydb.core.Flyway - Successfully applied 1 migration to schema "mortise", now at version v2 (execution time 00:00.123s)
```

## 验证步骤

### 1. 重新构建项目
```bash
mvn clean package -DskipTests
```

### 2. 启动应用
```bash
java -jar mortise-app/target/mortise.war
```

### 3. 检查启动日志
查找以下关键字：
- `WeChatMpConfiguration`
- `WeChatOpenConfiguration` 
- `mortise_wechat_account`

### 4. 验证数据库表
连接数据库，检查表是否创建：
```sql
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'mortise' 
AND table_name LIKE 'mortise_wechat%';
```

应该返回：
- `mortise_wechat_account`
- `mortise_wechat_config`

### 5. 检查配置数据
```sql
SELECT account_name, account_type, app_id, is_enabled 
FROM mortise_wechat_account;
```

默认会有两条记录（公众号和开放平台），但都是禁用状态（`is_enabled = 0`）。

## API 端点验证

启动成功后，可以访问以下端点：

### 1. Swagger UI
访问: `http://localhost:9999/mortise/swagger-ui.html`
查找 WeChat 相关的 API 接口。

### 2. WeChat 账号管理 API
```bash
# 获取所有账号
GET /api/v1/wechat/accounts

# 获取账号配置
GET /api/v1/wechat/accounts/{accountId}/config
```

### 3. WeChat 登录 API
```bash
# 获取扫码登录 URL（会因为配置未启用而返回错误，这是正常的）
GET /api/v1/wechat/login/qrcode-url?redirectUri=http://localhost:3000/callback
```

## 下一步配置

### 1. 启用账号配置
通过管理界面或直接更新数据库：
```sql
-- 启用默认公众号
UPDATE mortise_wechat_account SET is_enabled = 1 WHERE account_type = 'mp' AND is_default = 1;

-- 更新真实的 AppSecret
UPDATE mortise_wechat_config 
SET config_value = 'ENC(your_encrypted_app_secret)', is_encrypted = 1 
WHERE config_key = 'appSecret' 
AND account_id = (SELECT id FROM mortise_wechat_account WHERE account_type = 'mp' AND is_default = 1);
```

### 2. 配置环境变量
```bash
export ENCRYPTION_KEY=your-secret-key
```

> **说明**: 使用的是 `ENCRYPTION_KEY` 环境变量，这是 `mortise-core` 模块中 Jasypt 配置所期望的变量名。

### 3. 重启应用
重启后应该能看到微信服务初始化成功的日志。

## 故障排除

### 如果仍然看不到日志：
1. 检查 `pom.xml` 是否包含 `mortise-wechat` 依赖
2. 检查 `application-dev.yml` 中的 WeChat 配置
3. 检查数据库连接是否正常
4. 检查 Flyway 迁移是否执行成功

### 如果看到配置服务未启用的警告：
这是正常的！因为默认配置是禁用状态，需要手动启用具体账号。

### 如果看到数据库相关错误：
1. 确认数据库连接配置正确
2. 确认数据库用户有创建表的权限
3. 检查 Flyway 迁移脚本是否有语法错误

---
*更新日期: 2024-12-19*  
*负责人: GitHub Copilot*