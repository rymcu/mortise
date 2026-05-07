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

export interface WeChatSilentOpenIdResponse {
  openId?: string
  isBound?: boolean
  bound?: boolean
  memberId?: string | null
}


