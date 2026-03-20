/** Agent Chat 相关类型 */

export interface AgentChatMessage {
  id: string
  role: 'user' | 'assistant'
  parts: AgentChatPart[]
  time: string
}

export interface AgentChatPart {
  type: 'text'
  text: string
}

export interface AgentChatResponse {
  conversationId: string
  content: string
  intent: 'CHAT' | 'TOOL_CALL'
  modelType: string
  modelName: string
  toolCalls?: AgentToolCall[]
  tokenUsage?: AgentTokenUsage
  metadata?: Record<string, unknown>
}

export interface AgentToolCall {
  name: string
  arguments: Record<string, unknown>
  result?: string
}

export interface AgentTokenUsage {
  promptTokens: number
  completionTokens: number
  totalTokens: number
}

/** 后端 /app/agent/models 接口返回的提供商信息 */
export interface AgentProviderInfo {
  providerCode: string
  providerName: string
  models: AgentModelItem[]
}

/** 后端返回的单个模型信息 */
export interface AgentModelItem {
  modelName: string
  displayName: string
}

/** USelectMenu 选项格式 */
export interface AgentModelOption {
  label: string
  /** 格式: providerCode:modelName */
  value: string
  icon: string
}

/** 提供商代号 → 图标映射 */
export const PROVIDER_ICON_MAP: Record<string, string> = {
  openai: 'i-simple-icons-openai',
  anthropic: 'i-simple-icons-anthropic',
  deepseek: 'i-lucide-brain',
  dashscope: 'i-lucide-cloud',
  zhipu: 'i-lucide-sparkles',
  ollama: 'i-lucide-server',
}

/** 通用回退图标 */
export const DEFAULT_PROVIDER_ICON = 'i-lucide-cpu'
