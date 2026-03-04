# Mortise Product（通用产品目录模块）

`mortise-product` 是通用资源目录基础模块，提供分类树、条目注册表、多属性规格框架与生命周期状态机。
商品、固件、课程、设备型号均可复用同一套基础数据。

> **免费公共模块**：已纳入主仓默认构建，无需 Git Submodule。

## 模块定位

- **"产品是什么"** — 仅维护产品的描述型元数据（标题、分类、规格、SEO 等）
- **不关心"产品怎么卖/怎么用"** — 定价、库存、物流等交易属性由 `mortise-commerce` 扩展
- **可通过 SPI 扩展** — 自定义产品类型、校验规则、生命周期回调

## SPI 扩展点

| SPI 接口 | 用途 | 示例实现 |
|----------|------|---------|
| `ProductTypeProvider` | 注册自定义产品类型 | 电商注册 course/membership，IoT 注册 firmware |
| `ProductValidator` | 注入额外校验规则 | 电商要求上架前必须有定价 |
| `ProductLifecycleListener` | 监听产品状态变更 | 下架时同步停用 SKU 定价 |

## 子模块结构

```
mortise-product
├── mortise-product-domain        # 领域层：实体、枚举、SPI 接口
├── mortise-product-infra         # 基础设施层：Mapper、Flyway 迁移
├── mortise-product-application   # 应用层：Service 实现
├── mortise-product-admin         # 后台管理 API
└── mortise-product-api           # 客户端 API
```
