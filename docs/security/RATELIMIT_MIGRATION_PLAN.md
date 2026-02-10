# RateLimit 注解增强迁移方案

## 🎯 **目标**

将 `Resilience4jRateLimit` 的丰富功能合并到现有的 `RateLimit` 注解中，统一限流注解。

## 📊 **现状分析**

### **当前 RateLimit 注解**（mortise-web-support）
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {
    String name() default "default";
    String fallbackMessage() default "请求过于频繁，请稍后再试";
}
```

### **Resilience4jRateLimit 注解**（mortise-system）
```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Resilience4jRateLimit {
    String name() default "";
    int limitForPeriod() default 10;
    long refreshPeriodSeconds() default 1;
    long timeoutMillis() default 100;
    KeyType keyType() default KeyType.IP_AND_METHOD;
    String keyExpression() default "";
    String message() default "请求过于频繁，请稍后再试";
    int errorCode() default 429;
    boolean enableFallback() default false;
    String fallbackMethod() default "";
    
    enum KeyType { IP, METHOD, IP_AND_METHOD, USER_ID, IP_AND_USER_ID, CUSTOM }
}
```

## 🔄 **迁移策略**

### **方案：增强现有 RateLimit 注解**

**优点**：
- 保持向后兼容性
- 统一限流注解
- 保留已有的使用代码

**实施步骤**：

1. **增强 mortise-web-support 的 RateLimit 注解**
   - 添加 Resilience4j 的所有配置参数
   - 保持原有参数作为默认值
   - 添加 KeyType 枚举

2. **删除 mortise-system 的 Resilience4jRateLimit 注解**
   - 已确认当前项目中未使用

3. **验证现有使用不受影响**
   - 现有的 `@RateLimit(name="xxx")` 调用继续有效

## 📝 **增强后的 RateLimit 注解设计**

```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {
    
    // ========== 原有参数（保持兼容性） ==========
    
    /**
     * 限流器名称
     */
    String name() default "default";
    
    /**
     * 限流失败时的提示信息
     * @deprecated 使用 message() 替代
     */
    @Deprecated
    String fallbackMessage() default "";
    
    // ========== 新增 Resilience4j 功能 ==========
    
    /**
     * 周期内允许的请求数
     */
    int limitForPeriod() default 10;
    
    /**
     * 刷新周期（秒）
     */
    long refreshPeriodSeconds() default 1;
    
    /**
     * 超时时间（毫秒）
     */
    long timeoutMillis() default 100;
    
    /**
     * 限流key的生成策略
     */
    KeyType keyType() default KeyType.IP_AND_METHOD;
    
    /**
     * 自定义限流key表达式（SpEL）
     */
    String keyExpression() default "";
    
    /**
     * 限流失败时的错误消息
     */
    String message() default "请求过于频繁，请稍后再试";
    
    /**
     * 限流失败时的错误代码
     */
    int errorCode() default 429;
    
    /**
     * 是否启用降级处理
     */
    boolean enableFallback() default false;
    
    /**
     * 降级方法名
     */
    String fallbackMethod() default "";
    
    /**
     * Key生成类型枚举
     */
    enum KeyType {
        IP, METHOD, IP_AND_METHOD, USER_ID, IP_AND_USER_ID, CUSTOM
    }
}
```

## 🔧 **实施计划**

### **阶段 1：增强 RateLimit 注解**
- 修改 `mortise-web-support/annotation/RateLimit.java`
- 添加所有 Resilience4j 功能
- 保持向后兼容性

### **阶段 2：删除重复注解**
- 删除 `mortise-system/annotation/Resilience4jRateLimit.java`

### **阶段 3：验证现有功能**
- 确保现有的 `@RateLimit` 使用不受影响
- 编译验证

## ✅ **兼容性验证**

现有代码：
```java
@RateLimit(name = "auth-login")
```

增强后仍然有效：
```java
@RateLimit(name = "auth-login")  // ✅ 继续工作
```

新功能可选使用：
```java
@RateLimit(
    name = "auth-login",
    limitForPeriod = 5,
    refreshPeriodSeconds = 300,
    message = "登录请求过于频繁"
)  // ✅ 新功能
```

---

**准备开始迁移！** 🚀