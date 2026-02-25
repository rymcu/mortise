# packages/core-sdk Plan

Mortise 后端 API SDK（OSS 范围）。

## 责任范围

- 统一请求客户端
- API 模块封装
- DTO/VO 类型定义
- 错误模型标准化

## API 分域（仅 OSS）

- `auth`
- `system`
- `member`
- `file`
- `wechat`

## 约束

- 禁止引入 commerce/order/payment 相关接口。
- 所有接口路径以后端现有 `/api/v1/admin/**`、`/api/v1/app/**` 为准。

## 迭代计划

1. 建立基础 `request` 客户端与错误模型。
2. 封装 auth 与 oauth2 关键接口。
3. 封装 system/member/file/wechat API。
4. 增加类型测试与 API 变更检查。

## 完成标准

- 页面层不直接拼接 URL。
- 所有请求具备明确类型定义。
- OSS 边界通过 lint/脚本静态检查。
