# 微信配置数据库加载实现文档

## 📖 概述

本文档说明如何使用数据库动态加载微信配置，支持配置缓存、敏感信息加密、在线管理等特性。

## 🎯 核心特性

### 1. 数据库配置管理
- ✅ 配置存储在数据库表 `mortise_wechat_config`
- ✅ 支持微信公众号（mp）和开放平台（open）两种类型
- ✅ 配置变更无需重启应用

### 2. 敏感信息加密
- ✅ AppSecret、AES Key 等敏感信息支持加密存储
- ✅ 使用 Jasypt 加密，与项目统一
- ✅ 自动加解密，透明使用

### 3. 配置缓存
- ✅ 使用 Spring Cache 缓存配置
- ✅ 缓存键：`wechat:config:mp` 和 `wechat:config:open`
- ✅ 支持手动刷新缓存

### 4. 在线管理
- ✅ 提供 REST API 管理配置
- ✅ 支持配置测试验证
- ✅ 实时生效（刷新缓存）

## 📋 数据库表结构

### mortise_wechat_config 表

```sql
CREATE TABLE mortise.mortise_wechat_config (
    id BIGINT PRIMARY KEY,
    config_type VARCHAR(20) NOT NULL,    -- 'mp' 或 'open'
    config_key VARCHAR(50) NOT NULL,     -- 配置项键名
    config_value TEXT,                   -- 配置项值
    config_label VARCHAR(100),           -- 配置项描述
    is_encrypted INTEGER DEFAULT 0,      -- 是否加密
    sort_no INTEGER DEFAULT 0,
    status INTEGER DEFAULT 0,
    del_flag INTEGER DEFAULT 0,
    ...
);
```

### 初始化数据

```sql
-- 公众号配置
INSERT INTO mortise.mortise_wechat_config VALUES
    (1, 'mp', 'enabled', 'false', '是否启用', 0, 1, 0, ...),
    (2, 'mp', 'appId', '', '公众号AppID', 0, 2, 0, ...),
    (3, 'mp', 'appSecret', '', '公众号AppSecret', 1, 3, 0, ...);

-- 开放平台配置
INSERT INTO mortise.mortise_wechat_config VALUES
    (6, 'open', 'enabled', 'false', '是否启用', 0, 1, 0, ...),
    (7, 'open', 'appId', '', '开放平台AppID', 0, 2, 0, ...);
```

## 🔧 实现原理

### 1. 配置加载流程

```
启动 → WeChatMpConfiguration.wxMpService()
     → WeChatConfigService.loadMpConfig()
     → 从数据库查询配置
     → 解密敏感信息
     → 填充到 WeChatMpProperties
     → 缓存配置
     → 初始化 WxMpService
```

### 2. 配置刷新流程

```
管理员更新配置 → WeChatConfigController.updateConfig()
               → WeChatConfigManagementService.updateConfig()
               → 加密敏感信息（如果需要）
               → 更新数据库
               → 清除缓存
               → 下次访问重新加载
```

### 3. 敏感信息加密流程

```
保存时：明文 → Jasypt 加密 → 存入数据库
读取时：数据库 → Jasypt 解密 → 明文使用
```

## 📝 使用说明

### 1. 配置微信参数（数据库）

```sql
-- 更新公众号 AppID
UPDATE mortise.mortise_wechat_config
SET config_value = 'wx1234567890abcdef'
WHERE config_type = 'mp' AND config_key = 'appId';

-- 更新公众号 AppSecret（需加密）
UPDATE mortise.mortise_wechat_config
SET config_value = ENC('your-secret-here'),  -- 使用 Jasypt 加密
    is_encrypted = 1
WHERE config_type = 'mp' AND config_key = 'appSecret';

-- 启用公众号功能
UPDATE mortise.mortise_wechat_config
SET config_value = 'true'
WHERE config_type = 'mp' AND config_key = 'enabled';
```

### 2. 使用管理 API

#### 获取所有配置
```bash
GET /api/admin/wechat/config/list?configType=mp
```

#### 更新配置
```bash
PUT /api/admin/wechat/config/2
Content-Type: application/json

{
  "configValue": "wx1234567890abcdef"
}
```

#### 刷新缓存
```bash
POST /api/admin/wechat/config/refresh
```

#### 测试配置
```bash
# 测试公众号配置
POST /api/admin/wechat/config/test/mp

# 测试开放平台配置
POST /api/admin/wechat/config/test/open
```

### 3. 代码中使用

配置加载对业务代码透明，无需修改：

```java
@Service
@RequiredArgsConstructor
public class MyService {
    
    // 自动注入，从数据库加载
    private final WxMpService wxMpService;
    
    public void sendMessage() {
        // 直接使用，配置已自动加载
        wxMpService.getTemplateMsgService().sendTemplateMsg(...);
    }
}
```

## 🔐 敏感信息加密

### 1. 配置加密密钥

设置环境变量：

```bash
# Linux/Mac
export ENCRYPTION_KEY=your-secret-key

# Windows
set ENCRYPTION_KEY=your-secret-key

# Docker
-e ENCRYPTION_KEY=your-secret-key
```

### 2. 加密配置值

使用 Jasypt 命令行工具：

```bash
java -cp jasypt-1.9.3.jar \
  org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI \
  input="your-secret" \
  password="your-encryption-key" \
  algorithm=PBEWithMD5AndDES
```

或使用代码：

```java
@Autowired
private StringEncryptor stringEncryptor;

public void encryptSecret() {
    String encrypted = stringEncryptor.encrypt("your-secret");
    System.out.println("Encrypted: " + encrypted);
}
```

### 3. 存储加密后的值

```sql
UPDATE mortise.mortise_wechat_config
SET config_value = 'EncryptedValueHere',
    is_encrypted = 1
WHERE config_key = 'appSecret';
```

## 🎨 配置管理界面（建议）

可以开发一个管理界面：

```html
<form>
  <label>公众号 AppID</label>
  <input type="text" v-model="config.appId">
  
  <label>公众号 AppSecret</label>
  <input type="password" v-model="config.appSecret">
  
  <label>Token</label>
  <input type="text" v-model="config.token">
  
  <button @click="saveConfig">保存</button>
  <button @click="testConfig">测试连接</button>
  <button @click="refreshCache">刷新缓存</button>
</form>
```

## 📊 优势对比

### 配置文件方式

```yaml
wechat:
  mp:
    app-id: wx123456  # ❌ 硬编码
    app-secret: xxx   # ❌ 明文存储
```

❌ 需要重启应用  
❌ 不支持在线修改  
❌ 敏感信息暴露风险  

### 数据库方式

```sql
SELECT * FROM mortise_wechat_config WHERE config_type = 'mp';
```

✅ 无需重启应用  
✅ 支持在线管理  
✅ 敏感信息加密  
✅ 配置变更审计  
✅ 多环境隔离  

## 🚨 注意事项

1. **首次启动**：确保数据库迁移脚本已执行
2. **加密密钥**：生产环境必须设置 `ENCRYPTION_KEY`
3. **缓存刷新**：配置更新后记得刷新缓存
4. **权限控制**：配置管理 API 应添加权限验证
5. **备份**：定期备份配置数据

## 🔄 迁移指南

### 从配置文件迁移到数据库

1. **导出现有配置**
```bash
# 查看当前配置
cat application.yml | grep -A 10 wechat
```

2. **导入到数据库**
```sql
UPDATE mortise.mortise_wechat_config
SET config_value = 'wx123456'
WHERE config_type = 'mp' AND config_key = 'appId';
```

3. **禁用配置文件**
```yaml
wechat:
  mp:
    enabled: false  # 关闭配置文件方式
```

4. **启用数据库配置**
```sql
UPDATE mortise.mortise_wechat_config
SET config_value = 'true'
WHERE config_type = 'mp' AND config_key = 'enabled';
```

## 📚 相关文件

- 迁移脚本：`mortise-wechat/src/main/resources/db/migration/V1__Create_WeChat_Config_Table.sql`
- 实体类：`WeChatConfig.java`
- Mapper：`WeChatConfigMapper.java`
- 配置服务：`WeChatConfigService.java`
- 管理服务：`WeChatConfigManagementService.java`
- 管理接口：`WeChatConfigController.java`

## 🎉 总结

通过数据库动态加载配置，实现了：

✅ **灵活性**：配置变更无需重启  
✅ **安全性**：敏感信息加密存储  
✅ **可管理性**：支持在线配置管理  
✅ **可扩展性**：易于添加新配置项  
✅ **多环境**：支持不同环境不同配置  

适合生产环境使用！
