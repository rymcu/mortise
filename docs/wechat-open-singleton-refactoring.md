# 微信开放平台配置简化重构

## 重构日期
2025-10-06

## 重构背景

微信开放平台在一个项目下只有一个配置，不像公众号那样支持多个账号。因此之前的多账号设计是不必要的。

## 重构内容

### 1. WeChatOpenConfiguration 简化

#### 重构前（多账号设计）
```java
// ❌ 错误：创建了两个 Bean
@Bean
@Primary
public Optional<WxOpenService> wxOpenService() {
    // 默认服务
}

@Bean
public Optional<Map<Long, WxOpenService>> wxOpenServiceMap() {
    // 多账号服务容器（不需要！）
}
```

**问题：**
- 微信开放平台一个项目只有一个配置
- 多账号设计增加了不必要的复杂性
- 浪费内存和维护成本

#### 重构后（单例设计）
```java
// ✅ 正确：只创建一个单例 Bean
@Bean
@ConditionalOnProperty(prefix = "wechat.open", name = "enabled", havingValue = "true")
public Optional<WxOpenService> wxOpenService() {
    // 从数据库加载配置
    WeChatOpenProperties properties = configService.get().loadDefaultOpenConfig();
    
    if (properties == null || !properties.isEnabled()) {
        return Optional.empty();
    }
    
    WxOpenService service = createWxOpenService(properties);
    return Optional.of(service);
}
```

**改进：**
- 符合微信开放平台的实际使用场景
- 代码更简洁、易维护
- 移除了不必要的多账号逻辑

### 2. WeChatLoginService 简化

#### 重构前
```java
private final Optional<WxOpenService> wxOpenService; // 默认开放平台服务
private final Optional<Map<Long, WxOpenService>> wxOpenServiceMap; // 多账号开放平台服务（不需要！）
```

#### 重构后
```java
private final WeChatMpServiceUtil weChatMpServiceUtil; // 微信公众号服务工具类（支持多账号）
private final Optional<WxOpenService> wxOpenService; // 微信开放平台服务（单例）
```

**说明：**
- 移除了 `wxOpenServiceMap` 字段
- 注释明确说明：公众号支持多账号，开放平台单例
- 代码更清晰，避免混淆

## 架构对比

### 微信公众号（MP）架构
```
✅ 支持多账号
WxMpService (单例)
    ├─ setMultiConfigStorages(Map<String, WxMpConfigStorage>)
    └─ switchoverTo(appId)  // 切换账号
```

### 微信开放平台（Open）架构
```
✅ 一个项目一个配置
WxOpenService (单例)
    └─ 单一配置，无需账号切换
```

## 重构收益

### 1. 代码简化
- **删除代码：**~50 行
- **简化依赖：** 从 2 个 Bean 减少到 1 个
- **消除混淆：** 明确公众号和开放平台的差异

### 2. 性能提升
- **内存节省：** 不再创建无用的 Map 容器
- **启动优化：** 减少 Bean 初始化时间

### 3. 维护性提升
- **架构清晰：** 符合实际业务场景
- **易于理解：** 新开发者不会被多账号逻辑误导
- **降低复杂度：** 减少潜在 Bug

## 验证结果

```bash
mvn clean compile -pl mortise-wechat -am -DskipTests
```

✅ **BUILD SUCCESS** - Total time: 14.546 s

所有模块编译通过：
- mortise-common
- mortise-core
- mortise-cache
- mortise-auth
- mortise-wechat

## 最佳实践总结

### 公众号（MP）配置
```java
// ✅ 使用单例 + setMultiConfigStorages 支持多账号
@Bean
public Optional<WxMpService> wxMpService() {
    WxMpService service = new WxMpServiceImpl();
    Map<String, WxMpConfigStorage> configMap = loadAllMpConfigs();
    service.setMultiConfigStorages(configMap);
    return Optional.of(service);
}
```

### 开放平台（Open）配置
```java
// ✅ 使用单例，无需多账号支持
@Bean
public Optional<WxOpenService> wxOpenService() {
    WeChatOpenProperties properties = loadOpenConfig();
    WxOpenService service = createWxOpenService(properties);
    return Optional.of(service);
}
```

## 注意事项

1. **公众号与开放平台的区别**
   - 公众号：支持多个账号（如多个公司、多个产品）
   - 开放平台：一个项目一个配置（网站扫码登录）

2. **配置来源**
   - 两者都从数据库动态加载配置
   - 支持 `@ConditionalOnProperty` 控制是否启用

3. **Optional 包装**
   - 当数据库表不存在时不影响应用启动
   - 业务代码需要检查 `Optional.isPresent()`

## 后续工作

- [ ] 如果需要支持多个开放平台配置，需重新设计架构
- [ ] 完善 `createWxOpenService()` 方法的配置 API（当前 WxJava 开放平台 API 可能不稳定）
- [ ] 考虑添加开放平台配置的动态刷新机制

## 参考资料

- [WxJava 官方文档 - 公众号快速开始](https://github.com/binarywang/WxJava/wiki/MP_Quick-Start)
- [WxJava 官方 Demo - 公众号多账号](https://github.com/binarywang/weixin-java-mp-demo)
- [微信开放平台官方文档](https://open.weixin.qq.com/cgi-bin/frame?t=home/web_tmpl)

---

**作者：** GitHub Copilot  
**审核：** @ronger  
**版本：** v1.0
