/**
 * 消息收件箱 / 咨询会话相关类型
 *
 * 与后端 SessionVO / MessageVO 对齐
 */

/** 会话状态枚举值 */
export type SessionStatus = 0 | 1 | 2 // OPEN=0, CLOSED=1, WAITING=2

/**
 * 用户咨询会话（左侧列表条目）
 * 对应后端 SessionVO
 */
export interface ChatSession {
  id: number
  userId: number
  userName: string | null
  userAvatar: string | null
  status: SessionStatus
  contextType: string | null
  contextId: number | null
  contextTitle: string | null
  lastMessage: string | null
  unreadCount: number
  updatedTime: string | null
}

/**
 * 会话消息条目
 *
 * parts 格式与 Nuxt UI UChatMessage 兼容（AI SDK v5 文本块子集）
 * 对应后端 MessageVO
 */
export interface InboxChatMessage {
  id: string
  sessionId: number
  /** 'user' = 访客来信，'assistant' = 客服回复 */
  role: 'user' | 'assistant'
  parts: Array<{ type: 'text'; text: string }>
  /** 后端 createdTime 格式化后的显示时间 */
  time: string
  senderId: number | null
}
