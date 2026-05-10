import type { ApiInvoker } from './admin'
import type {
  GlobalResult,
  DesktopAuthorizeResponse,
  MemberClientSession,
  OAuth2AuthUrlResponse,
  OAuth2LoginResponse,
  OAuth2QRCodeResponse,
  OAuth2QRCodeStateResponse,
  PageResult,
  QRCodeLoginResponse,
  TokenRefreshResponse,
  WeChatSilentOpenIdResponse,
} from './types'

function assertSuccess<T>(
  response: GlobalResult<T> | null | undefined,
  fallbackMessage: string,
) {
  if (!response || response.code !== 200) {
    throw new Error(response?.message || fallbackMessage)
  }

  return response.data
}

function normalizeOAuth2LoginResponse(value: OAuth2LoginResponse | null | undefined): OAuth2LoginResponse | null {
  if (!value) {
    return null
  }

  return {
    ...value,
    memberId: value.memberId == null ? null : String(value.memberId),
  }
}

function normalizeWeChatSilentOpenIdResponse(
  value: WeChatSilentOpenIdResponse | null | undefined,
): WeChatSilentOpenIdResponse | null {
  if (!value) {
    return null
  }

  return {
    ...value,
    memberId: value.memberId == null ? null : String(value.memberId),
  }
}

export async function registerAppMember(
  api: ApiInvoker,
  payload: Record<string, unknown>,
): Promise<string | null> {
  const response = await api<GlobalResult<unknown>>('/api/v1/app/auth/register', {
    method: 'POST',
    body: payload,
  })

  const memberId = assertSuccess(response, '会员注册失败')
  return memberId == null ? null : String(memberId)
}

export async function loginAppMember<T extends Record<string, unknown>>(
  api: ApiInvoker,
  payload: Record<string, unknown>,
): Promise<T | null> {
  const response = await api<GlobalResult<T>>('/api/v1/app/auth/login', {
    method: 'POST',
    body: payload,
  })

  return assertSuccess(response, '会员登录失败') ?? null
}

export async function loginAppMemberByPhone<T extends Record<string, unknown>>(
  api: ApiInvoker,
  payload: Record<string, unknown>,
): Promise<T | null> {
  const response = await api<GlobalResult<T>>('/api/v1/app/auth/login-by-phone', {
    method: 'POST',
    body: payload,
  })

  return assertSuccess(response, '手机号登录失败') ?? null
}

export async function refreshAppMemberToken<T extends Record<string, unknown>>(
  api: ApiInvoker,
  payload: Record<string, unknown>,
): Promise<T | null> {
  const response = await api<GlobalResult<T>>('/api/v1/app/auth/refresh-token', {
    method: 'POST',
    body: payload,
  })

  return assertSuccess(response, '刷新 token 失败') ?? null
}

export async function refreshAppMemberTokenByJwt(
  api: ApiInvoker,
  authHeader: string,
): Promise<TokenRefreshResponse | null> {
  const response = await api<GlobalResult<TokenRefreshResponse>>(
    '/api/v1/app/auth/refresh-token-by-jwt',
    {
      method: 'POST',
      headers: {
        Authorization: authHeader,
      },
    },
  )

  return assertSuccess(response, '刷新 token 失败') ?? null
}

export async function fetchAppMemberProfile<T extends Record<string, unknown>>(
  api: ApiInvoker,
  options?: Record<string, unknown>,
): Promise<T | null> {
  const response = await api<GlobalResult<T>>('/api/v1/app/auth/profile', {
    method: 'GET',
    ...options,
  })

  return assertSuccess(response, '获取会员信息失败') ?? null
}

export async function updateAppMemberProfile(
  api: ApiInvoker,
  payload: Record<string, unknown>,
): Promise<boolean> {
  const response = await api<GlobalResult<boolean>>('/api/v1/app/auth/profile', {
    method: 'PUT',
    body: payload,
  })

  return Boolean(assertSuccess(response, '更新会员信息失败'))
}

export async function updateAppMemberPassword(
  api: ApiInvoker,
  payload: Record<string, unknown>,
): Promise<boolean> {
  const response = await api<GlobalResult<boolean>>('/api/v1/app/auth/password', {
    method: 'PUT',
    body: payload,
  })

  return Boolean(assertSuccess(response, '更新密码失败'))
}

export async function updateAppMemberUsername(
  api: ApiInvoker,
  payload: Record<string, unknown>,
): Promise<boolean> {
  const response = await api<GlobalResult<boolean>>('/api/v1/app/auth/username', {
    method: 'PUT',
    body: payload,
  })

  return Boolean(assertSuccess(response, '更新用户名失败'))
}

export async function sendAppMemberCode(
  api: ApiInvoker,
  payload: Record<string, unknown>,
): Promise<boolean> {
  const response = await api<GlobalResult<boolean>>('/api/v1/app/auth/send-code', {
    method: 'POST',
    body: payload,
  })

  return Boolean(assertSuccess(response, '发送验证码失败'))
}

export async function verifyAppMemberCode(
  api: ApiInvoker,
  payload: Record<string, unknown>,
): Promise<boolean> {
  const response = await api<GlobalResult<boolean>>('/api/v1/app/auth/verify-code', {
    method: 'POST',
    body: payload,
  })

  return Boolean(assertSuccess(response, '验证码校验失败'))
}

export async function resetAppMemberPassword(
  api: ApiInvoker,
  payload: Record<string, unknown>,
): Promise<boolean> {
  const response = await api<GlobalResult<boolean>>('/api/v1/app/auth/reset-password', {
    method: 'POST',
    body: payload,
  })

  return Boolean(assertSuccess(response, '重置密码失败'))
}

export async function fetchWechatOAuthQrCode(
  api: ApiInvoker,
  registrationId = 'wechat-app',
): Promise<OAuth2QRCodeResponse | null> {
  const response = await api<GlobalResult<OAuth2QRCodeResponse>>(
    '/api/v1/app/oauth2/wechat/qrcode',
    {
      method: 'GET',
      query: { registrationId },
    },
  )

  return assertSuccess(response, '获取微信二维码失败') ?? null
}

export async function fetchOAuthQrCodeState(
  api: ApiInvoker,
  state: string,
): Promise<OAuth2QRCodeStateResponse | null> {
  const response = await api<GlobalResult<OAuth2QRCodeStateResponse>>(
    `/api/v1/app/oauth2/qrcode/state/${state}`,
    {
      method: 'GET',
    },
  )

  return assertSuccess(response, '获取二维码状态失败') ?? null
}

export async function fetchWechatMobileAuthUrl(
  api: ApiInvoker,
  registrationId = 'wechat-app',
  scope = 'snsapi_userinfo',
): Promise<OAuth2AuthUrlResponse | null> {
  const response = await api<GlobalResult<OAuth2AuthUrlResponse>>(
    '/api/v1/app/oauth2/wechat/mobile/auth-url',
    {
      method: 'GET',
      query: { registrationId, scope },
    },
  )

  return assertSuccess(response, '获取微信移动授权地址失败') ?? null
}

export async function fetchOAuthAuthorizationUrl(
  api: ApiInvoker,
  registrationId: string,
): Promise<OAuth2AuthUrlResponse | null> {
  const response = await api<GlobalResult<OAuth2AuthUrlResponse>>(
    `/api/v1/app/oauth2/auth-url/${registrationId}`,
    {
      method: 'GET',
    },
  )

  return assertSuccess(response, '获取 OAuth2 授权地址失败') ?? null
}

export async function exchangeAppOAuthState(
  api: ApiInvoker,
  state: string,
): Promise<OAuth2LoginResponse | null> {
  const response = await api<GlobalResult<OAuth2LoginResponse>>('/api/v1/app/oauth2/callback', {
    method: 'GET',
    query: { state },
  })

  return normalizeOAuth2LoginResponse(assertSuccess(response, '兑换 OAuth2 登录态失败'))
}

export async function createWechatLoginQrCode(
  api: ApiInvoker,
  payload?: { expireSeconds?: number; appId?: string },
): Promise<QRCodeLoginResponse | null> {
  const response = await api<GlobalResult<QRCodeLoginResponse>>('/api/v1/app/oauth2/mp/qrcode', {
    method: 'POST',
    query: payload ?? {},
  })

  return assertSuccess(response, '创建微信登录二维码失败') ?? null
}

export async function fetchWechatSilentAuthUrl(
  api: ApiInvoker,
  payload: { redirectUri: string; appId?: string },
): Promise<OAuth2AuthUrlResponse | null> {
  const response = await api<GlobalResult<OAuth2AuthUrlResponse>>(
    '/api/v1/app/oauth2/wechat/silent/auth-url',
    {
      method: 'GET',
      query: payload,
    },
  )

  return assertSuccess(response, '获取微信静默授权地址失败') ?? null
}

export async function fetchWechatSilentOpenId(
  api: ApiInvoker,
  code: string,
  appId?: string,
): Promise<WeChatSilentOpenIdResponse | null> {
  const response = await api<GlobalResult<WeChatSilentOpenIdResponse>>(
    '/api/v1/app/oauth2/wechat/silent/openid',
    {
      method: 'GET',
      query: {
        code,
        appId,
      },
    },
  )

  return normalizeWeChatSilentOpenIdResponse(assertSuccess(response, '获取微信 openId 失败'))
}

export async function authorizeDesktopClient(
  api: ApiInvoker,
  query: Record<string, unknown>,
): Promise<DesktopAuthorizeResponse | null> {
  const response = await api<GlobalResult<DesktopAuthorizeResponse>>(
    '/api/v1/app/desktop/oauth/authorize',
    {
      method: 'GET',
      query,
    },
  )

  return assertSuccess(response, '授权 Rodak 失败') ?? null
}

function normalizeMemberClientSession(value: MemberClientSession): MemberClientSession {
  return {
    ...value,
    id: String(value.id),
  }
}

export async function fetchMemberClientSessions(
  api: ApiInvoker,
): Promise<MemberClientSession[]> {
  const response = await api<GlobalResult<MemberClientSession[]>>(
    '/api/v1/app/security/client-sessions',
    {
      method: 'GET',
    },
  )

  return (assertSuccess(response, '获取设备会话失败') ?? []).map(normalizeMemberClientSession)
}

export async function revokeMemberClientSession(
  api: ApiInvoker,
  sessionId: string,
): Promise<boolean> {
  const response = await api<GlobalResult<boolean>>(
    `/api/v1/app/security/client-sessions/${encodeURIComponent(sessionId)}`,
    {
      method: 'DELETE',
    },
  )

  return Boolean(assertSuccess(response, '撤销设备会话失败'))
}

export async function uploadAppFile<T extends Record<string, unknown>>(
  api: ApiInvoker,
  formData: FormData,
): Promise<T | null> {
  const response = await api<GlobalResult<T>>('/api/v1/app/files', {
    method: 'POST',
    body: formData,
  })

  return assertSuccess(response, '上传文件失败') ?? null
}

export async function fetchFamilyList<T>(
  api: ApiInvoker,
  query?: Record<string, unknown>,
): Promise<PageResult<T> | null> {
  const response = await api<GlobalResult<PageResult<T>>>('/api/v1/app/families', {
    method: 'GET',
    query,
  })

  return assertSuccess(response, '获取家庭列表失败') ?? null
}
