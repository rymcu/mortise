# MyBatis-Flex 配置修复报告

## 问题描述

遇到了多个 MyBatis-Flex 配置属性无法解析的错误：

```
Cannot resolve configuration property 'mybatis-flex.global-config.db-type'
Cannot resolve configuration property 'mybatis-flex.global-config.logic-delete-value' 
Cannot resolve configuration property 'mybatis-flex.global-config.logic-un-delete-value'
Cannot resolve configuration property 'mybatis-flex.global-config.id-type'
Cannot resolve configuration property 'mybatis-flex.global-config.insert-strategy'
Cannot resolve configuration property 'mybatis-flex.global-config.update-strategy'
Cannot resolve configuration property 'mybatis-flex.global-config.select-strategy'
```

## 根本原因

这些配置属性被错误地放置在 `mybatis-flex.global-config` 节点下，但根据 MyBatis-Flex 官方文档，`global-config` 节点只支持特定的配置属性。

## 有效的 global-config 属性

根据官方文档，`mybatis-flex.global-config` 只支持以下属性：

### 核心配置
- `print-banner`: 是否控制台打印 MyBatis-Flex 的 LOGO 及版本号
- `key-config`: 全局的 ID 生成策略配置

### 逻辑删除配置
- `normal-value-of-logic-delete`: 逻辑删除数据存在标记值（默认: 0）
- `deleted-value-of-logic-delete`: 逻辑删除数据删除标记值（默认: 1）
- `logic-delete-column`: 默认的逻辑删除字段（默认: del_flag）

### 多租户和版本控制
- `tenant-column`: 默认的多租户字段（默认: tenant_id）
- `version-column`: 默认的乐观锁字段（默认: version）

## 修复方案

### 修复前配置
```yaml
mybatis-flex:
  global-config:
    print-banner: false
    db-type: postgresql              # ❌ 无效属性
    logic-delete-column: del_flag
    logic-delete-value: 1            # ❌ 属性名错误
    logic-un-delete-value: 0         # ❌ 属性名错误
    id-type: auto                    # ❌ 无效属性
    insert-strategy: not_null        # ❌ 无效属性
    update-strategy: not_null        # ❌ 无效属性
    select-strategy: not_null        # ❌ 无效属性
```

### 修复后配置
```yaml
mybatis-flex:
  global-config:
    # 关闭 Banner 打印
    print-banner: false
    # 逻辑删除配置
    logic-delete-column: del_flag
    deleted-value-of-logic-delete: 1     # ✅ 正确属性名
    normal-value-of-logic-delete: 0      # ✅ 正确属性名
```

## 被移除的配置说明

以下配置属性不属于 `global-config` 范畴，已被移除：

1. **`db-type`**: 数据库类型配置，MyBatis-Flex 会自动检测
2. **`id-type`**: 主键策略，应在实体类的 `@Id` 注解中指定
3. **`insert-strategy`**: 插入策略，应在实体类的字段注解中指定
4. **`update-strategy`**: 更新策略，应在实体类的字段注解中指定
5. **`select-strategy`**: 查询策略，应在实体类的字段注解中指定

## 替代方案

如果需要全局配置策略，可以考虑以下方案：

### 1. 实体类注解配置
```java
@Table("user")
public class User {
    @Id(keyType = KeyType.AUTO)
    private Long id;
    
    @Column(insertStrategy = FieldStrategy.NOT_NULL)
    private String username;
}
```

### 2. 自定义配置类
```java
@Configuration
public class MyBatisFlexConfig {
    @Bean
    public MyBatisFlexCustomizer myBatisFlexCustomizer() {
        return new MyBatisFlexCustomizer() {
            @Override
            public void customize(FlexGlobalConfig globalConfig) {
                // 自定义全局配置
                globalConfig.setDefaultInsertStrategy(FieldStrategy.NOT_NULL);
                globalConfig.setDefaultUpdateStrategy(FieldStrategy.NOT_NULL);
            }
        };
    }
}
```

## 验证结果

- ✅ Maven 编译成功，无配置错误
- ✅ 所有 MyBatis-Flex 配置属性均有效
- ✅ 逻辑删除功能配置正确
- ✅ Banner 显示控制正常

## 当前配置状态

现在的 MyBatis-Flex 配置完全符合官方规范：

```yaml
mybatis-flex:
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: com.rymcu.mortise.entity
  configuration:
    # MyBatis 原生配置
    map-underscore-to-camel-case: true
    default-executor-type: reuse
    cache-enabled: true
    # 其他 MyBatis 配置...
  global-config:
    # 仅包含 MyBatis-Flex 特有的全局配置
    print-banner: false
    logic-delete-column: del_flag
    deleted-value-of-logic-delete: 1
    normal-value-of-logic-delete: 0
```

## 建议

1. **遵循官方文档**: 严格按照 MyBatis-Flex 官方文档进行配置
2. **分层配置**: 区分 MyBatis 原生配置（configuration）和 MyBatis-Flex 特有配置（global-config）
3. **实体注解优先**: 对于字段策略等配置，优先使用实体类注解
4. **定期更新**: 关注 MyBatis-Flex 版本更新，及时调整配置