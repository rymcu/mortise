<script setup lang="ts">
defineProps<{
  open: boolean
  editingId: string | null
  form: {
    name: string
    description: string
    ownerType: string
    triggerLogic: string
    conditionLogic: string
    triggers: { triggerType: string, config: Record<string, unknown>, sortOrder: number }[]
    conditions: { conditionType: string, config: Record<string, unknown>, sortOrder: number }[]
    actions: { actionType: string, config: Record<string, unknown>, sortOrder: number }[]
  }
  saving: boolean
  triggerTypeOptions: { label: string, value: string }[]
  conditionTypeOptions: { label: string, value: string }[]
  actionTypeOptions: { label: string, value: string }[]
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  submit: []
  addTrigger: []
  removeTrigger: [index: number]
  addCondition: []
  removeCondition: [index: number]
  addAction: []
  removeAction: [index: number]
}>()

const triggerLogicOptions = [
  { label: '任一触发 (ANY)', value: 'ANY' },
  { label: '全部满足 (ALL)', value: 'ALL' }
]
const conditionLogicOptions = [
  { label: 'AND（全部满足）', value: 'AND' },
  { label: 'OR（任一满足）', value: 'OR' }
]
</script>

<template>
  <UModal :open="open" :title="editingId ? '编辑场景' : '新建场景'" :ui="{ content: 'sm:max-w-2xl' }" @update:open="emit('update:open', $event)">
    <template #body>
      <div class="space-y-5 p-4 max-h-[70vh] overflow-y-auto">
        <!-- 基础信息 -->
        <UFormField label="场景名称" required>
          <UInput v-model="form.name" placeholder="例如: 高温自动降温" class="w-full" />
        </UFormField>
        <div class="grid grid-cols-2 gap-4">
          <UFormField label="触发逻辑">
            <USelect :model-value="form.triggerLogic" :items="triggerLogicOptions" value-key="value" class="w-full" @update:model-value="form.triggerLogic = String($event ?? 'ANY')" />
          </UFormField>
          <UFormField label="条件逻辑">
            <USelect :model-value="form.conditionLogic" :items="conditionLogicOptions" value-key="value" class="w-full" @update:model-value="form.conditionLogic = String($event ?? 'AND')" />
          </UFormField>
        </div>
        <UFormField label="描述">
          <UTextarea v-model="form.description" :rows="2" placeholder="可选说明" class="w-full" />
        </UFormField>

        <!-- 触发器 -->
        <div class="rounded-lg border border-gray-200 dark:border-gray-700 p-3 space-y-3">
          <div class="flex items-center justify-between">
            <span class="text-sm font-medium text-gray-700 dark:text-gray-300">触发器</span>
            <UButton size="xs" icon="i-lucide-plus" variant="outline" @click="emit('addTrigger')">添加</UButton>
          </div>
          <div v-for="(trigger, idx) in form.triggers" :key="idx" class="flex items-center gap-2 rounded-md border border-gray-100 dark:border-gray-800 p-2">
            <USelect :model-value="trigger.triggerType" :items="triggerTypeOptions" value-key="value" class="w-44" @update:model-value="trigger.triggerType = String($event)" />
            <span class="flex-1 text-xs text-[var(--ui-text-muted)]">配置 (JSON) 将在 PRD-2 提供可视化编辑</span>
            <UButton size="xs" icon="i-lucide-trash-2" color="error" variant="ghost" @click="emit('removeTrigger', idx)" />
          </div>
          <p v-if="form.triggers.length === 0" class="text-xs text-gray-400">至少添加一个触发器</p>
        </div>

        <!-- 条件 -->
        <div class="rounded-lg border border-gray-200 dark:border-gray-700 p-3 space-y-3">
          <div class="flex items-center justify-between">
            <span class="text-sm font-medium text-gray-700 dark:text-gray-300">条件（可选）</span>
            <UButton size="xs" icon="i-lucide-plus" variant="outline" @click="emit('addCondition')">添加</UButton>
          </div>
          <div v-for="(cond, idx) in form.conditions" :key="idx" class="flex items-center gap-2 rounded-md border border-gray-100 dark:border-gray-800 p-2">
            <USelect :model-value="cond.conditionType" :items="conditionTypeOptions" value-key="value" class="w-44" @update:model-value="cond.conditionType = String($event)" />
            <span class="flex-1 text-xs text-[var(--ui-text-muted)]">配置 (JSON) 将在 PRD-2 提供可视化编辑</span>
            <UButton size="xs" icon="i-lucide-trash-2" color="error" variant="ghost" @click="emit('removeCondition', idx)" />
          </div>
          <p v-if="form.conditions.length === 0" class="text-xs text-gray-400">无附加条件</p>
        </div>

        <!-- 动作 -->
        <div class="rounded-lg border border-gray-200 dark:border-gray-700 p-3 space-y-3">
          <div class="flex items-center justify-between">
            <span class="text-sm font-medium text-gray-700 dark:text-gray-300">执行动作</span>
            <UButton size="xs" icon="i-lucide-plus" variant="outline" @click="emit('addAction')">添加</UButton>
          </div>
          <div v-for="(action, idx) in form.actions" :key="idx" class="flex items-center gap-2 rounded-md border border-gray-100 dark:border-gray-800 p-2">
            <USelect :model-value="action.actionType" :items="actionTypeOptions" value-key="value" class="w-44" @update:model-value="action.actionType = String($event)" />
            <span class="flex-1 text-xs text-[var(--ui-text-muted)]">配置 (JSON) 将在 PRD-2 提供可视化编辑</span>
            <UButton size="xs" icon="i-lucide-trash-2" color="error" variant="ghost" @click="emit('removeAction', idx)" />
          </div>
          <p v-if="form.actions.length === 0" class="text-xs text-gray-400">至少添加一个执行动作</p>
        </div>

        <div class="flex justify-end gap-2 pt-2">
          <UButton color="neutral" variant="outline" @click="emit('update:open', false)">取消</UButton>
          <UButton :loading="saving" icon="i-lucide-save" @click="emit('submit')">
            {{ editingId ? '保存修改' : '创建场景' }}
          </UButton>
        </div>
      </div>
    </template>
  </UModal>
</template>

