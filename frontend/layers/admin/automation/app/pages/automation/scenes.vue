<script setup lang="ts">
import type { AutomationScene } from '~/types/automation-admin'
import { formatDateTime } from '~/utils/automation-admin'

const { $api } = useNuxtApp()
const toast = useToast()

const {
  scenes, loading, saving, modalOpen, editingId, form, statusFilter,
  loadScenes, openCreate, openEdit, submitForm, deleteScene, toggleStatus
} = useScenes($api, toast)

const columns = [
  { key: 'name', label: '场景名称' },
  { key: 'status', label: '状态' },
  { key: 'triggerLogic', label: '触发逻辑' },
  { key: 'conditionLogic', label: '条件逻辑' },
  { key: 'updatedTime', label: '更新时间' }
]

const statusFilterOptions = [
  { label: '全部', value: undefined },
  { label: '已启用', value: 1 },
  { label: '已禁用', value: 0 }
]

const triggerTypeOptions = [
  { label: '设备属性', value: 'DEVICE_PROPERTY' },
  { label: '设备事件', value: 'DEVICE_EVENT' },
  { label: '定时', value: 'TIMER' },
  { label: '设备状态', value: 'DEVICE_STATUS' }
]
const conditionTypeOptions = [
  { label: '设备属性', value: 'DEVICE_PROPERTY' },
  { label: '时间范围', value: 'TIME_RANGE' }
]
const actionTypeOptions = [
  { label: '设置属性', value: 'SET_PROPERTY' },
  { label: '调用服务', value: 'INVOKE_SERVICE' },
  { label: '发送通知', value: 'SEND_NOTIFICATION' },
  { label: '触发场景', value: 'TRIGGER_SCENE' },
  { label: '延时', value: 'DELAY' }
]

await loadScenes()

function addTrigger() { form.value.triggers.push({ triggerType: 'DEVICE_PROPERTY', config: {}, sortOrder: form.value.triggers.length }) }
function removeTrigger(idx: number) { form.value.triggers.splice(idx, 1) }
function addCondition() { form.value.conditions.push({ conditionType: 'DEVICE_PROPERTY', config: {}, sortOrder: form.value.conditions.length }) }
function removeCondition(idx: number) { form.value.conditions.splice(idx, 1) }
function addAction() { form.value.actions.push({ actionType: 'SET_PROPERTY', config: {}, sortOrder: form.value.actions.length }) }
function removeAction(idx: number) { form.value.actions.splice(idx, 1) }
</script>

<template>
  <UDashboardPanel id="automation-scenes">
    <template #header>
      <UDashboardNavbar title="场景管理">
        <template #leading>
          <UDashboardSidebarCollapse />
        </template>
      </UDashboardNavbar>
    </template>
    <template #body>
      <AdminPagedTableCard
        :columns="columns"
        :rows="scenes as unknown as Record<string, unknown>[]"
        :loading="loading" :error-message="''" :total="scenes.length"
        :page-num="1" :page-size="100" :total-page="1" :has-next="false" :has-previous="false"
        keyword="" empty-text="暂无自动化场景" show-actions
        @refresh="loadScenes"
      >
        <template #toolbar>
          <div class="flex items-center gap-3">
            <USelect :model-value="statusFilter ?? undefined" :items="statusFilterOptions" value-key="value" placeholder="全部状态" class="w-36" @update:model-value="statusFilter = $event as number | undefined; loadScenes()" />
            <UButton icon="i-lucide-plus" @click="openCreate">新建场景</UButton>
          </div>
        </template>
        <template #cell-status="{ row }">
          <UBadge :color="row.status === 1 ? 'success' : 'neutral'" variant="subtle">
            {{ row.status === 1 ? '已启用' : '已禁用' }}
          </UBadge>
        </template>
        <template #cell-updatedTime="{ row }">{{ formatDateTime(String(row.updatedTime ?? '')) }}</template>
        <template #actions="{ row }">
          <div class="flex gap-1">
            <UButton variant="ghost" size="xs" :icon="row.status === 1 ? 'i-lucide-pause' : 'i-lucide-play'" @click="toggleStatus(String(row.id), row.status === 1 ? 0 : 1)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </UButton>
            <UButton variant="ghost" size="xs" icon="i-lucide-pencil" @click="openEdit(String(row.id))">编辑</UButton>
            <UButton variant="ghost" size="xs" color="error" icon="i-lucide-trash-2" @click="deleteScene(String(row.id))">删除</UButton>
          </div>
        </template>
      </AdminPagedTableCard>

      <AutomationAdminSceneFormModal
        :open="modalOpen" :editing-id="editingId" :form="form" :saving="saving"
        :trigger-type-options="triggerTypeOptions"
        :condition-type-options="conditionTypeOptions"
        :action-type-options="actionTypeOptions"
        @update:open="modalOpen = $event"
        @add-trigger="addTrigger" @remove-trigger="removeTrigger"
        @add-condition="addCondition" @remove-condition="removeCondition"
        @add-action="addAction" @remove-action="removeAction"
        @submit="submitForm"
      />
    </template>
  </UDashboardPanel>
</template>

