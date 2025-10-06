# WeChat 模块重构方案

## 重构目标

1. 使用 Service 和 ServiceImpl 模式
2. 使用 mybatis-flex 简化代码
3. 移除旧表结构支持
4. Controller 统一返回 GlobalResult
5. 列表实现分页
6. 完善 API 文档

## 重构架构

### 1. Service 层结构

```
service/
├── WeChatAccountService.java          # 账号管理服务接口
├── WeChatConfigService.java           # 配置加载服务接口
├── WeChatLoginService.java            # 登录服务接口
├── WeChatMessageService.java          # 消息服务接口
└── impl/
    ├── WeChatAccountServiceImpl.java  # 账号管理服务实现
    ├── WeChatConfigServiceImpl.java   # 配置加载服务实现
    ├── WeChatLoginServiceImpl.java    # 登录服务实现
    └── WeChatMessageServiceImpl.java  # 消息服务实现
```

### 2. Service 职责划分

#### WeChatAccountService
- **职责**: 账号和配置的CRUD管理
- **功能**:
  - 账号分页查询、创建、更新、删除
  - 设置默认账号、启用/禁用账号
  - 配置的保存、批量保存、删除
  - 加密/解密处理
  - 缓存刷新

#### WeChatConfigService
- **职责**: 配置加载和使用
- **功能**:
  - 加载公众号配置（默认/按ID/按AppID）
  - 加载开放平台配置（默认/按ID）
  - 配置缓存
  - 配置解密

#### WeChatLoginService
- **职责**: 微信登录业务
- **功能**:
  - 扫码登录
  - 授权回调处理
  - 用户信息获取

#### WeChatMessageService
- **职责**: 消息处理业务
- **功能**:
  - 文本消息发送
  - 图文消息发送
  - 模板消息发送

### 3. Controller 重构

#### WeChatAccountController
- **路径**: `/api/v1/admin/wechat/accounts`
- **权限**: `@PreAuthorize("hasRole('ADMIN')")`
- **功能**:
  - `GET /` - 分页查询账号列表
  - `GET /{id}` - 获取账号详情
  - `POST /` - 创建账号
  - `PUT /{id}` - 更新账号
  - `DELETE /{id}` - 删除账号
  - `PATCH /{id}/default` - 设置默认账号
  - `PATCH /{id}/status` - 启用/禁用账号
  - `GET /{id}/configs` - 获取账号配置列表
  - `POST /{id}/configs` - 批量保存配置
  - `DELETE /{id}/configs/{key}` - 删除配置

#### WeChatLoginController
- **路径**: `/api/v1/wechat/login`
- **权限**: 公开接口
- **功能**:
  - `GET /qrcode` - 获取扫码登录二维码
  - `GET /callback` - 授权回调
  - `POST /bind` - 绑定微信账号

#### WeChatMessageController
- **路径**: `/api/v1/admin/wechat/messages`
- **权限**: `@PreAuthorize("hasRole('ADMIN')")`
- **功能**:
  - `POST /text` - 发送文本消息
  - `POST /news` - 发送图文消息
  - `POST /template` - 发送模板消息

#### WeChatPortalController
- **路径**: `/api/v1/wechat/portal/{appId}`
- **权限**: 公开接口
- **功能**:
  - `GET /` - 验证服务器地址
  - `POST /` - 接收微信消息

### 4. DTO/VO 定义

#### 请求对象（Request）

**CreateAccountRequest**
```java
{
    "accountType": "mp",
    "accountName": "公众号名称",
    "appId": "wxxxxxxxxxxx",
    "appSecret": "xxxxxxxxxxxxx",
    "isDefault": true,
    "isEnabled": true,
    "remark": "备注"
}
```

**UpdateAccountRequest**
```java
{
    "accountName": "新名称",
    "isDefault": false,
    "isEnabled": true,
    "remark": "新备注"
}
```

**BatchSaveConfigsRequest**
```java
{
    "configs": [
        {
            "configKey": "token",
            "configValue": "xxxxxx",
            "configLabel": "Token",
            "isEncrypted": false
        },
        {
            "configKey": "aesKey",
            "configValue": "xxxxxx",
            "configLabel": "AES Key",
            "isEncrypted": true
        }
    ]
}
```

#### 响应对象（Response）

**WeChatAccountVO**
```java
{
    "id": 1,
    "accountType": "mp",
    "accountName": "公众号名称",
    "appId": "wxxxxxxxxxxx",
    "appSecret": "***",  // 脱敏
    "isDefault": 1,
    "isEnabled": 1,
    "status": 0,
    "remark": "备注",
    "createdTime": "2025-10-06 16:00:00",
    "updatedTime": "2025-10-06 16:00:00"
}
```

**WeChatConfigVO**
```java
{
    "id": 1,
    "accountId": 1,
    "configKey": "token",
    "configValue": "xxxxxx",  // 如果加密则脱敏
    "configLabel": "Token",
    "isEncrypted": 0,
    "sortNo": 1
}
```

### 5. 数据库表结构

#### mortise_wechat_account（账号表）
```sql
CREATE TABLE mortise_wechat_account (
    id BIGSERIAL PRIMARY KEY,
    account_type VARCHAR(20) NOT NULL,
    account_name VARCHAR(100) NOT NULL,
    app_id VARCHAR(50) NOT NULL UNIQUE,
    app_secret VARCHAR(500) NOT NULL,
    is_default SMALLINT DEFAULT 0,
    is_enabled SMALLINT DEFAULT 1,
    status SMALLINT DEFAULT 0,
    del_flag SMALLINT DEFAULT 0,
    remark VARCHAR(500),
    created_by BIGINT,
    created_time TIMESTAMP,
    updated_by BIGINT,
    updated_time TIMESTAMP
);

CREATE INDEX idx_account_type ON mortise_wechat_account(account_type);
CREATE INDEX idx_app_id ON mortise_wechat_account(app_id);
```

#### mortise_wechat_config（配置表）
```sql
CREATE TABLE mortise_wechat_config (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL,
    config_key VARCHAR(100) NOT NULL,
    config_type VARCHAR(20),
    config_value VARCHAR(1000),
    config_label VARCHAR(100),
    is_encrypted SMALLINT DEFAULT 0,
    sort_no INT DEFAULT 0,
    status SMALLINT DEFAULT 0,
    del_flag SMALLINT DEFAULT 0,
    remark VARCHAR(500),
    created_by BIGINT,
    created_time TIMESTAMP,
    updated_by BIGINT,
    updated_time TIMESTAMP,
    UNIQUE(account_id, config_key)
);

CREATE INDEX idx_account_id ON mortise_wechat_config(account_id);
```

### 6. Mapper 优化

使用 mybatis-flex 的 QueryWrapper 简化查询：

```java
// 查询启用的账号
List<WeChatAccount> accounts = accountMapper.selectListByQuery(
    QueryWrapper.create()
        .where(WECHAT_ACCOUNT.ACCOUNT_TYPE.eq(accountType))
        .and(WECHAT_ACCOUNT.IS_ENABLED.eq(1))
        .and(WECHAT_ACCOUNT.DEL_FLAG.eq(0))
        .orderBy(WECHAT_ACCOUNT.IS_DEFAULT.desc(), WECHAT_ACCOUNT.ID.asc())
);

// 分页查询
Page<WeChatAccount> page = accountMapper.paginate(pageNum, pageSize,
    QueryWrapper.create()
        .where(WECHAT_ACCOUNT.ACCOUNT_TYPE.eq(search.getAccountType()))
        .and(WECHAT_ACCOUNT.IS_ENABLED.eq(search.getIsEnabled()))
        .orderBy(WECHAT_ACCOUNT.CREATED_TIME.desc())
);
```

### 7. 缓存策略

使用 Spring Cache 注解：

```java
// 配置缓存
@Cacheable(value = "wechat:config", key = "'mp:' + #accountId", unless = "#result == null")
public WeChatMpProperties loadMpConfigByAccountId(Long accountId) {
    // ...
}

// 缓存失效
@CacheEvict(value = "wechat:config", allEntries = true)
public void refreshCache() {
    // ...
}
```

### 8. API 文档

使用 Swagger/OpenAPI 注解：

```java
@Tag(name = "微信账号管理", description = "微信账号配置管理相关接口")
@RestController
@RequestMapping("/api/v1/admin/wechat/accounts")
public class WeChatAccountController {

    @Operation(summary = "获取账号列表", description = "分页查询微信账号")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @GetMapping
    public GlobalResult<Page<WeChatAccount>> pageAccounts(
        @Parameter(description = "查询条件") @Valid WeChatAccountSearch search
    ) {
        // ...
    }
}
```

### 9. 实施步骤

#### 第一步：创建 Service 接口（已完成）
- ✅ WeChatAccountService
- ⬜ WeChatConfigService
- ⬜ WeChatLoginService
- ⬜ WeChatMessageService

#### 第二步：实现 ServiceImpl
- ⬜ WeChatAccountServiceImpl
- ⬜ WeChatConfigServiceImpl
- ⬜ WeChatLoginServiceImpl
- ⬜ WeChatMessageServiceImpl

#### 第三步：重构 Controller
- ⬜ WeChatAccountController
- ⬜ WeChatLoginController
- ⬜ WeChatMessageController
- ⬜ WeChatPortalController

#### 第四步：创建 DTO/VO
- ⬜ Request 对象
- ⬜ Response 对象（VO）

#### 第五步：测试和文档
- ⬜ 单元测试
- ⬜ API 文档完善
- ⬜ 集成测试

## 使用示例

### 创建账号

```http
POST /api/v1/admin/wechat/accounts
Content-Type: application/json

{
    "accountType": "mp",
    "accountName": "RYMCU公众号",
    "appId": "wxabcdefg123456",
    "appSecret": "secret123456",
    "isDefault": true,
    "isEnabled": true
}
```

### 查询账号列表

```http
GET /api/v1/admin/wechat/accounts?pageNum=1&pageSize=10&accountType=mp
```

### 保存配置

```http
POST /api/v1/admin/wechat/accounts/1/configs
Content-Type: application/json

{
    "configs": [
        {
            "configKey": "token",
            "configValue": "mytoken123",
            "configLabel": "Token",
            "isEncrypted": false
        },
        {
            "configKey": "aesKey",
            "configValue": "myaeskey456",
            "configLabel": "AES Key",
            "isEncrypted": true
        }
    ]
}
```

## 注意事项

1. **加密处理**: AppSecret 和敏感配置需要加密存储
2. **缓存一致性**: 更新配置后要清除缓存
3. **默认账号**: 同类型只能有一个默认账号
4. **软删除**: 使用 del_flag 标记删除
5. **审计字段**: 记录创建人、创建时间等
6. **事务管理**: 批量操作要使用事务
7. **异常处理**: 统一的异常处理机制
8. **日志记录**: 关键操作要记录日志

## 后续优化

1. 添加配置版本管理
2. 支持配置导入/导出
3. 添加配置变更历史
4. 支持配置模板
5. 添加配置验证功能
6. 支持多环境配置
