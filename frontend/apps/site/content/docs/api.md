---
title: API 参考
description: Mortise 后端 API 接口规范与主要模块接口说明
order: 3
---

# API 参考

Mortise 提供 RESTful API，所有接口统一前缀 `/mortise/api/v1`。

## 响应格式

所有接口统一返回格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | int | 状态码，200 成功，非 200 表示错误 |
| message | string | 提示信息 |
| data | any | 响应数据 |

## 认证

受保护的接口需要在请求头携带 Bearer Token：

```http
Authorization: Bearer <access_token>
```

Token 通过登录接口获取，本地开发中当 Token 过期时前端会自动调用刪新接口。

## 认证模块

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/auth/login` | 账号密码登录 |
| POST | `/api/v1/auth/refresh-token` | 刷新 Token |
| GET | `/api/v1/auth/oauth2/callback` | OAuth2 陆式回调 |
| POST | `/api/v1/auth/sms/send` | 发送手机验证码 |
| POST | `/api/v1/auth/sms/login` | 手机验证码登录 |
| GET | `/api/v1/auth/qrcode` | 生成扫码登录二维码 |

## 系统模块（管理端）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/admin/system/users` | 用户列表 |
| GET | `/api/v1/admin/system/roles` | 角色列表 |
| GET | `/api/v1/admin/system/menus` | 菜单列表 |
| GET | `/api/v1/admin/system/dict-types` | 字典类型列表 |
| GET | `/api/v1/admin/system/site-config/public` | 展示站点公开配置 |

## 社区模块

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/community/articles` | 文章列表（支持分页、关键词、标签过滤） |
| GET | `/api/v1/community/articles/{id}` | 文章详情 |
| POST | `/api/v1/community/articles` | 发布文章（需登录） |
| PUT | `/api/v1/community/articles/{id}` | 更新文章（需登录） |
| GET | `/api/v1/community/articles/{id}/comments` | 评论列表 |
| POST | `/api/v1/community/articles/{id}/comments` | 发表评论（需登录） |

## 会员模块

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/member/register` | 会员注册 |
| GET | `/api/v1/member/profile` | 获取当前会员信息 |
| PUT | `/api/v1/member/profile` | 更新会员信息 |

## 文件模块

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/file/upload` | 上传文件 |
| POST | `/api/v1/file/upload/chunk` | 分片上传 |
| GET | `/api/v1/file/{id}` | 获取文件信息 |
| DELETE | `/api/v1/file/{id}` | 删除文件 |

## 分页参数

列表接口统一支持分页参数：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|---------|------|
| pageNum | int | 1 | 页码 |
| pageSize | int | 10 | 每页条数 |

## 在线文档

启动后端后访问 `http://localhost:9999/mortise/swagger-ui/index.html` 查看完整 **Swagger UI** API 文档，支持在线调试。接口按 Admin / API 分组展示。
