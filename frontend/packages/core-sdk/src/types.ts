export interface GlobalResult<T> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  records: T[]
  pageNumber: number
  pageSize: number
  totalPage: number
  totalRow: number
  maxPageSize?: number
  optimizeCountQuery?: boolean
  hasNext?: boolean
  hasPrevious?: boolean
  hasRecords?: boolean
  offset?: number
}

export interface OAuth2AuthUrlResponse {
  authorizationUrl?: string
  state?: string
  appId?: string | null
  clientId?: string | null
  redirectUri?: string | null
  scope?: string | null
  authType?: string | null
}

export interface OAuth2QRCodeResponse {
  authorizationUri?: string
  qrCodeUrl?: string
  state?: string
  appId?: string | null
  clientId?: string | null
  redirectUri?: string | null
  scope?: string | null
}

export interface OAuth2QRCodeStateResponse {
  state?: string
  qrcodeState?: number
  status?: number
}

export interface OAuth2LoginResponse {
  memberId?: string | null
  token?: string
  refreshToken?: string
  tokenType?: string
  expiresIn?: number
  refreshExpiresIn?: number
  nickname?: string | null
  avatarUrl?: string | null
  username?: string | null
  openId?: string | null
  unionId?: string | null
  thirdPartyNickname?: string | null
  thirdPartyAvatarUrl?: string | null
  isNewUser?: boolean | null
  isBound?: boolean | null
}

export interface QRCodeLoginResponse {
  sceneStr?: string
  qrCodeUrl?: string
  ticket?: string
  expireSeconds?: number
  showQrCodeUrl?: string
}

export interface TokenRefreshResponse {
  token?: string
  refreshToken?: string
  tokenType?: string
  expiresIn?: number
  refreshExpiresIn?: number
}

export interface DesktopAuthorizeResponse {
  redirectUri?: string
}

export interface DesktopTokenResponse {
  memberId?: string | number | null
  username?: string | null
  nickname?: string | null
  avatarUrl?: string | null
  token?: string
  refreshToken?: string
  tokenType?: string
  accessTokenExpiryMs?: number
  refreshTokenExpiryMs?: number
  sessionId?: string | number | null
  clientId?: string | null
}

export type MemberClientSessionStatus = 'active' | 'revoked' | string

export interface MemberClientSession {
  id: string
  clientId?: string | null
  clientName?: string | null
  deviceName?: string | null
  status: MemberClientSessionStatus
  lastActiveAt?: string | null
  revokedAt?: string | null
  createdTime?: string | null
  current?: boolean | null
}

export interface WeChatSilentOpenIdResponse {
  openId?: string
  isBound?: boolean
  bound?: boolean
  memberId?: string | null
}


