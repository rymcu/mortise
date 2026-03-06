/**
 * apps/site 侧 IM 咨询相关类型
 */

/** 咨询会话信息（创建成功后保存到本地） */
export interface SiteSession {
  id: number
  status: number
}

/**
 * 站点侧聊天消息
 * parts 兼容 Nuxt UI UChatMessage
 */
export interface SiteChatMessage {
  id: string
  /** 'user' = 用户发送，'assistant' = 客服回复 */
  role: 'user' | 'assistant'
  parts: Array<{ type: 'text'; text: string }>
  time: string
}

/** 发起会话的上下文（用于标识用户在咨询哪个产品/服务） */
export interface ConsultContext {
  contextType?: string
  contextId?: number
  /** 前端展示用的预设问题 */
  subject?: string
}

/** 历史会话列表项 */
export interface SiteSessionItem {
  id: number
  status: number
  lastMessage: string | null
  updatedTime: string
  contextType: string | null
  contextId: number | null
}

/** 会话状态常量 */
export const SESSION_STATUS = {
  OPEN: 0,
  CLOSED: 1,
  WAITING: 2,
} as const
