# apps/web Plan

用户端（会员端/H5），承载会员认证与个人业务操作。

## 业务范围（OSS）

- 会员注册
- 账号密码登录
- 手机验证码登录（如启用）
- OAuth2 登录（微信/GitHub/Google/Logto 按后端配置）
- OAuth2 state 回调兑换 token
- 个人中心
- 文件相关用户能力（按后端开放接口）

## 路由约定

- `/app/auth/login`
- `/app/auth/register`
- `/app/auth/callback`
- `/app/profile`
- `/app/files`
- `/app/oauth2/qrcode-status`

## 鉴权流程计划

1. **统一会话模型**
   - 定义 accessToken/refreshToken/expiresAt/user。
2. **密码登录流程**
   - 调用 `/api/v1/app/auth/login`。
   - 建立会话并跳转业务页。
3. **OAuth2 流程**
   - 调用 `/api/v1/app/oauth2/auth-url/{registrationId}` 获取跳转地址。
   - 第三方登录后回到 `/app/auth/callback`。
   - 使用 `state` 调用 `/api/v1/app/oauth2/callback` 兑换 token。
4. **续期与容错**
   - 401 场景触发单飞刷新。
   - 刷新失败清理会话并回登录页。

## 完成标准

- 两种登录方式统一进入同一会话状态。
- 回调异常、state 过期、refresh 失败均有可感知反馈。
- 不出现 admin/商业域页面耦合。
