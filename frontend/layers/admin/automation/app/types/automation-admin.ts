// ===== 自动化场景 =====

export interface AutomationScene {
  id: string
  name?: string
  description?: string
  status?: number
  ownerType?: string
  ownerId?: string
  triggerLogic?: string
  conditionLogic?: string
  triggers?: AutomationTrigger[]
  conditions?: AutomationCondition[]
  actions?: AutomationAction[]
  createdTime?: string
  updatedTime?: string
}

export interface AutomationTrigger {
  id?: string
  sceneId?: string
  triggerType?: string
  config?: Record<string, unknown>
  sortOrder?: number
}

export interface AutomationCondition {
  id?: string
  sceneId?: string
  conditionType?: string
  config?: Record<string, unknown>
  sortOrder?: number
}

export interface AutomationAction {
  id?: string
  sceneId?: string
  actionType?: string
  config?: Record<string, unknown>
  sortOrder?: number
}

export interface AutomationSceneFormState {
  name: string
  description: string
  ownerType: string
  triggerLogic: string
  conditionLogic: string
  triggers: { triggerType: string, config: Record<string, unknown>, sortOrder: number }[]
  conditions: { conditionType: string, config: Record<string, unknown>, sortOrder: number }[]
  actions: { actionType: string, config: Record<string, unknown>, sortOrder: number }[]
}

