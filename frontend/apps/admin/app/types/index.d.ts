import type { AvatarProps } from '@nuxt/ui'

export type UserStatus = 'subscribed' | 'unsubscribed' | 'bounced'
export type SaleStatus = 'paid' | 'failed' | 'refunded'

export interface User {
  id: number
  name: string
  email: string
  avatar?: AvatarProps
  status: UserStatus
  location: string
}

export interface Mail {
  id: number
  unread?: boolean
  from: User
  subject: string
  body: string
  date: string
}

export interface Member {
  name: string
  username: string
  role: 'member' | 'owner'
  avatar: AvatarProps
}

export interface Stat {
  title: string
  icon: string
  value: number | string
  variation: number
  formatter?: (value: number) => string
}

export interface Sale {
  id: string
  date: string
  status: SaleStatus
  email: string
  amount: number
}

export interface Notification {
  id: number
  unread?: boolean
  sender: User
  body: string
  date: string
}

export type Period = 'daily' | 'weekly' | 'monthly'

export interface Range {
  start: Date
  end: Date
}

export interface ProfileInfo {
  nickname: string
  avatar: string | null
  email: string | null
  account: string
  bio: string | null
}

/** 表单字段 UI 渲染类型（与后端 FormFieldType 对应） */
export type FormFieldType = 'TEXT' | 'PASSWORD' | 'NUMBER' | 'BOOLEAN' | 'EMAIL' | 'SELECT' | 'IMAGE'

export interface ChannelFieldOption {
  label: string
  value: string
}

/** 通用动态表单字段定义（可复用于通知渠道、网站配置等场景） */
export interface FormFieldDef {
  key: string
  label: string
  type: FormFieldType
  required: boolean
  placeholder?: string
  defaultValue?: string
  options?: ChannelFieldOption[]
}

/** @deprecated 使用 FormFieldDef 代替 */
export type ChannelFieldDef = FormFieldDef

export interface ChannelConfigVO {
  channel: string
  label: string
  enabled: boolean
  schema: FormFieldDef[]
  values: Record<string, string>
}

export interface ChannelConfigSaveRequest {
  enabled: boolean
  values: Record<string, string>
}

// ─── 网站配置类型 ──────────────────────────────────────────────────────────

export interface SiteConfigGroupVO {
  group: string
  label: string
  schema: FormFieldDef[]
  values: Record<string, string>
}

export interface SiteConfigSaveRequest {
  values: Record<string, string>
}

export interface SiteConfigPublicVO {
  values: Record<string, string>
}

// ─── 文件管理类型 ──────────────────────────────────────────────────────────────

export interface FileDetail {
  id: number
  url: string
  size?: number
  filename?: string
  originalFilename?: string
  basePath?: string
  path?: string
  ext?: string
  contentType?: string
  platform?: string
  thUrl?: string
  createTime?: string
}

// ─── 日志类型 ─────────────────────────────────────────────────────────────────

export interface OperationLog {
  id: number
  traceId?: string
  clientType?: string
  module?: string
  operation?: string
  operatorId?: number
  operatorAccount?: string
  operateTime?: string
  requestUri?: string
  requestMethod?: string
  params?: string
  result?: string
  ip?: string
  duration?: number
  success?: boolean
  errorMsg?: string
}

export interface ApiLog {
  id: number
  traceId?: string
  clientType?: string
  apiDescription?: string
  className?: string
  methodName?: string
  userId?: number
  username?: string
  requestTime?: string
  requestUri?: string
  requestMethod?: string
  queryString?: string
  requestBody?: string
  responseBody?: string
  httpStatus?: number
  clientIp?: string
  userAgent?: string
  duration?: number
  success?: boolean
  errorMsg?: string
}

// ─── 监控类型 ────────────────────────────────────────────────────────────────

export interface HealthComponent {
  status: string
  details?: Record<string, unknown>
}

export interface HealthResponse {
  status: string
  components?: Record<string, HealthComponent>
}

export interface MetricMeasurement {
  statistic: string
  value: number
}

export interface MetricResponse {
  name: string
  measurements: MetricMeasurement[]
}

export interface InfoResponse {
  app?: {
    name?: string
    version?: string
    description?: string
  }
  java?: {
    version?: string
  }
  [key: string]: unknown
}
