import { fetchAdminGet, fetchAdminPost, fetchAdminPut, fetchAdminDelete, fetchAdminPatch } from '@mortise/core-sdk'
import type { AutomationScene, AutomationSceneFormState } from '~/types/automation-admin'
import { normalizeAutomationScene, createEmptySceneFormState } from '~/utils/automation-admin'

const API = '/api/v1/admin/automation/scenes'

export function useScenes(
  $api: ReturnType<typeof useNuxtApp>['$api'],
  toast: ReturnType<typeof useToast>
) {
  const scenes = ref<AutomationScene[]>([])
  const loading = ref(false)
  const saving = ref(false)
  const modalOpen = ref(false)
  const editingId = ref<string | null>(null)
  const form = ref<AutomationSceneFormState>(createEmptySceneFormState())
  const statusFilter = ref<number | undefined>(undefined)

  async function loadScenes() {
    loading.value = true
    try {
      const params = statusFilter.value !== undefined ? `?status=${statusFilter.value}` : ''
      const data = await fetchAdminGet<unknown[]>($api, `${API}${params}`)
      scenes.value = Array.isArray(data) ? data.map(normalizeAutomationScene) : []
    }
    catch { scenes.value = [] }
    finally { loading.value = false }
  }

  function openCreate() {
    editingId.value = null
    form.value = createEmptySceneFormState()
    modalOpen.value = true
  }

  async function openEdit(id: string) {
    editingId.value = id
    try {
      const data = await fetchAdminGet<unknown>($api, `${API}/${id}`)
      const scene = normalizeAutomationScene(data)
      form.value = {
        name: scene.name || '', description: scene.description || '',
        ownerType: scene.ownerType || 'PLATFORM',
        triggerLogic: scene.triggerLogic || 'ANY',
        conditionLogic: scene.conditionLogic || 'AND',
        triggers: (scene.triggers || []).map(t => ({
          triggerType: t.triggerType || '', config: t.config || {}, sortOrder: t.sortOrder ?? 0
        })),
        conditions: (scene.conditions || []).map(c => ({
          conditionType: c.conditionType || '', config: c.config || {}, sortOrder: c.sortOrder ?? 0
        })),
        actions: (scene.actions || []).map(a => ({
          actionType: a.actionType || '', config: a.config || {}, sortOrder: a.sortOrder ?? 0
        }))
      }
      modalOpen.value = true
    }
    catch (error) {
      toast.add({ title: '加载场景失败', description: error instanceof Error ? error.message : '', color: 'error', icon: 'i-lucide-alert-circle' })
    }
  }

  async function submitForm() {
    saving.value = true
    try {
      const payload = { ...form.value }
      if (editingId.value) {
        await fetchAdminPut($api, `${API}/${editingId.value}`, payload)
      } else {
        await fetchAdminPost($api, API, payload)
      }
      toast.add({ title: editingId.value ? '场景已更新' : '场景已创建', color: 'success', icon: 'i-lucide-check-circle' })
      modalOpen.value = false
      await loadScenes()
    }
    catch (error) {
      toast.add({ title: '保存失败', description: error instanceof Error ? error.message : '', color: 'error', icon: 'i-lucide-alert-circle' })
    }
    finally { saving.value = false }
  }

  async function deleteScene(id: string) {
    try {
      await fetchAdminDelete($api, `${API}/${id}`)
      toast.add({ title: '场景已删除', color: 'success', icon: 'i-lucide-check-circle' })
      await loadScenes()
    }
    catch (error) {
      toast.add({ title: '删除失败', description: error instanceof Error ? error.message : '', color: 'error', icon: 'i-lucide-alert-circle' })
    }
  }

  async function toggleStatus(id: string, status: number) {
    try {
      await fetchAdminPatch($api, `${API}/${id}/status`, { body: { status } })
      toast.add({ title: status === 1 ? '场景已启用' : '场景已禁用', color: 'success', icon: 'i-lucide-check-circle' })
      await loadScenes()
    }
    catch (error) {
      toast.add({ title: '操作失败', description: error instanceof Error ? error.message : '', color: 'error', icon: 'i-lucide-alert-circle' })
    }
  }

  return {
    scenes, loading, saving, modalOpen, editingId, form, statusFilter,
    loadScenes, openCreate, openEdit, submitForm, deleteScene, toggleStatus
  }
}

