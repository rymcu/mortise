import type { AutomationScene, AutomationSceneFormState } from '../types/automation-admin'

function asRecord(value: unknown): Record<string, unknown> {
  return (typeof value === 'object' && value !== null && !Array.isArray(value))
    ? value as Record<string, unknown>
    : {}
}
function toIdString(value: unknown): string {
  return value == null ? '' : String(value)
}
function toOptionalString(value: unknown): string | undefined {
  return value == null ? undefined : String(value)
}
function toNullableNumber(value: unknown): number | undefined {
  if (value == null) return undefined
  const n = Number(value)
  return Number.isFinite(n) ? n : undefined
}

export function normalizeAutomationScene(value: unknown): AutomationScene {
  const s = asRecord(value)
  return {
    id: toIdString(s.id),
    name: toOptionalString(s.name),
    description: toOptionalString(s.description),
    status: toNullableNumber(s.status),
    ownerType: toOptionalString(s.ownerType),
    ownerId: toIdString(s.ownerId),
    triggerLogic: toOptionalString(s.triggerLogic),
    conditionLogic: toOptionalString(s.conditionLogic),
    triggers: Array.isArray(s.triggers) ? s.triggers.map(t => asRecord(t) as unknown as AutomationScene['triggers'] extends (infer T)[] | undefined ? T : never) : [],
    conditions: Array.isArray(s.conditions) ? s.conditions.map(c => asRecord(c) as unknown as AutomationScene['conditions'] extends (infer T)[] | undefined ? T : never) : [],
    actions: Array.isArray(s.actions) ? s.actions.map(a => asRecord(a) as unknown as AutomationScene['actions'] extends (infer T)[] | undefined ? T : never) : [],
    createdTime: toOptionalString(s.createdTime),
    updatedTime: toOptionalString(s.updatedTime)
  }
}

export function createEmptySceneFormState(): AutomationSceneFormState {
  return {
    name: '', description: '', ownerType: 'PLATFORM',
    triggerLogic: 'ANY', conditionLogic: 'AND',
    triggers: [], conditions: [], actions: []
  }
}

export function formatDateTime(value: string): string {
  if (!value) return '-'
  try { return new Date(value).toLocaleString('zh-CN') }
  catch { return value }
}

