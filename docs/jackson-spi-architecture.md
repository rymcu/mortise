# Jackson 配置模块化架构设计文档

## 📅 设计日期
2025-10-01

## 🎯 设计目标
解决 GitHub 上 JacksonConfig 中业务代码耦合问题，实现基于 SPI 的模块化 Jackson 配置架构。

---

## 📋 问题分析

### **原始问题**
1. **模块归属不明确**：Jackson 配置散布在不同位置
2. **业务代码耦合**：通用配置与业务逻辑（如字典翻译）混合
3. **扩展性差**：新增序列化逻辑需要修改核心配置类
4. **职责不清**：违反单一职责原则

### **GitHub 上的 JacksonConfig 问题**
```java
// ❌ 业务代码直接耦合在通用配置中
public class JacksonConfig {
    private final DictService dictService; // 业务依赖

    // 基础配置 + 业务配置混合
    jsonMapper.setAnnotationIntrospector(new DictAnnotationIntrospector(dictService));
}
```

---

## 🏗️ **解决方案：SPI 模块化架构**

### **1. 架构分层**

```
┌─────────────────────────────────────────────────────────────┐
│                   mortise-web (Web 层)                      │
│  ┌─────────────────────────────────────────────────────────┐│
│  │              JacksonConfig                              ││
│  │   • 基础配置（日期、数值、容错）                          ││
│  │   • SPI 发现和应用机制                                   ││
│  │   • 配置器排序和管理                                     ││
│  └─────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
                             ↑
                   通过 SPI 接口扩展
                             ↑
┌─────────────────────────────────────────────────────────────┐
│               JacksonConfigurer SPI 接口                    │
│   • 定义配置扩展规范                                         │
│   • 支持优先级排序                                          │
│   • 支持条件启用/禁用                                        │
└─────────────────────────────────────────────────────────────┘
                             ↑
              各业务模块实现具体配置器
                             ↑
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│  mortise-system │  │  mortise-auth   │  │   其他模块      │
│ DictJackson     │  │ SecurityJackson │  │  CustomJackson  │
│ Configurer      │  │ Configurer      │  │  Configurer     │
│ • 字典翻译       │  │ • 敏感数据脱敏   │  │  • 自定义逻辑    │
└─────────────────┘  └─────────────────┘  └─────────────────┘
```

### **2. 模块职责划分**

| 模块 | 职责 | 包含内容 |
|------|------|----------|
| **mortise-web** | Jackson 核心配置 | 基础序列化规则、SPI 管理器、通用配置 |
| **mortise-system** | 业务字典翻译 | `@DictFormat` 注解处理、字典值转换 |
| **mortise-auth** | 安全相关序列化 | 敏感数据脱敏、OAuth2 序列化等 |
| **其他业务模块** | 领域特定序列化 | 各自的业务序列化逻辑 |

---

## 🛠️ **技术实现**

### **核心 SPI 接口**

```java
public interface JacksonConfigurer extends Ordered {
    void configureObjectMapper(ObjectMapper objectMapper);
    default int getOrder() { return LOWEST_PRECEDENCE; }
    default boolean isEnabled() { return true; }
}
```

### **主配置类架构**

```java
@Configuration
public class JacksonConfig {
    private final List<JacksonConfigurer> jacksonConfigurers;

    @Bean @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = createBaseObjectMapper();
        
        // 应用 SPI 扩展
        applySpiConfigurations(mapper);
        
        return mapper;
    }
}
```

### **业务配置器示例**

```java
@Component
@ConditionalOnClass(DictService.class)
public class DictJacksonConfigurer implements JacksonConfigurer {
    @Override
    public void configureObjectMapper(ObjectMapper objectMapper) {
        objectMapper.setAnnotationIntrospector(
            new DictAnnotationIntrospector(dictService)
        );
    }
    
    @Override
    public int getOrder() { return 200; } // 业务扩展优先级
}
```

---

## ✨ **架构优势**

### **1️⃣ 模块化解耦**
- ✅ 基础配置与业务逻辑分离
- ✅ 各业务模块管理自己的序列化逻辑
- ✅ 依赖关系清晰，易于维护

### **2️⃣ 高扩展性**
- ✅ 新增业务序列化无需修改核心配置
- ✅ 支持条件启用/禁用配置器
- ✅ 支持配置优先级排序

### **3️⃣ 职责单一**
- ✅ 每个配置器职责明确
- ✅ 符合单一职责原则
- ✅ 便于单元测试

### **4️⃣ 向后兼容**
- ✅ 不影响现有序列化逻辑
- ✅ 渐进式迁移
- ✅ 配置热插拔

---

## 📝 **配置器开发指南**

### **优先级规范**
| 优先级范围 | 用途 | 示例 |
|------------|------|------|
| `100-199` | 基础配置 | 日期格式、数值精度 |
| `200-299` | 业务扩展 | 字典翻译、数据转换 |
| `300-399` | 安全相关 | 敏感数据处理、加密 |
| `400+` | 其他扩展 | 自定义业务逻辑 |

### **开发步骤**
1. **实现接口**：继承 `JacksonConfigurer`
2. **添加注解**：`@Component` + 条件注解
3. **配置逻辑**：实现 `configureObjectMapper` 方法
4. **设置优先级**：重写 `getOrder` 方法
5. **条件控制**：重写 `isEnabled` 方法（可选）

### **最佳实践**
```java
@Component
@ConditionalOnClass(YourService.class)
@ConditionalOnProperty(value = "your.feature.enabled", havingValue = "true")
public class YourJacksonConfigurer implements JacksonConfigurer {
    
    @Override
    public void configureObjectMapper(ObjectMapper objectMapper) {
        // 你的配置逻辑
    }
    
    @Override
    public int getOrder() {
        return 250; // 业务扩展优先级
    }
}
```

---

## 🔄 **迁移指南**

### **从原始 JacksonConfig 迁移**

1. **保留基础配置** → `mortise-web/JacksonConfig`
2. **业务逻辑拆分** → 各自模块的 `XxxJacksonConfigurer`
3. **依赖解耦** → 通过 SPI 接口通信
4. **逐步迁移** → 一个业务一个配置器

### **迁移检查清单**
- [ ] 基础 Jackson 配置移至 `mortise-web`
- [ ] 业务序列化逻辑拆分到各模块
- [ ] 创建对应的 `JacksonConfigurer` 实现
- [ ] 添加条件注解确保模块间解耦
- [ ] 设置合理的配置优先级
- [ ] 编写单元测试验证配置

---

## 🎉 **总结**

### **解决的核心问题**
1. ✅ **模块归属明确**：Jackson 配置位于 `mortise-web`
2. ✅ **业务代码分离**：各模块管理自己的序列化逻辑
3. ✅ **高度可扩展**：基于 SPI 的插件化架构
4. ✅ **职责清晰**：单一职责，易于维护

### **实现的技术价值**
- **可维护性**：模块化架构，降低耦合度
- **可扩展性**：插件化设计，支持业务快速扩展
- **可测试性**：单一职责，便于单元测试
- **可配置性**：支持条件启用和优先级控制

**作者：** GitHub Copilot  
**日期：** 2025-10-01  
**状态：** ✅ 架构设计完成，代码实现完成，编译验证通过