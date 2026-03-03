<script setup lang="ts">
const { $api } = useNuxtApp()
const toast = useToast()

// 用户缓存表单
const userIdInput = ref('')
const clearingUserCache = ref(false)

// 字典缓存表单
const dictTypeInput = ref('')
const clearingDictCache = ref(false)
const clearingAllDictCache = ref(false)
const showClearAllDictConfirm = ref(false)

async function clearUserCache() {
  const userId = userIdInput.value.trim()
  if (!userId) {
    toast.add({ title: '请输入用户 ID', color: 'warning' })
    return
  }
  clearingUserCache.value = true
  try {
    await $api<unknown>(`/api/v1/admin/system/cache/user/${userId}`, { method: 'DELETE' })
    toast.add({ title: '用户缓存已清除', description: `用户 ID: ${userId}`, color: 'success' })
    userIdInput.value = ''
  } catch (e) {
    toast.add({ title: '清除失败', description: e instanceof Error ? e.message : '', color: 'error' })
  } finally {
    clearingUserCache.value = false
  }
}

async function clearDictCache() {
  const dictType = dictTypeInput.value.trim()
  if (!dictType) {
    toast.add({ title: '请输入字典类型', color: 'warning' })
    return
  }
  clearingDictCache.value = true
  try {
    await $api<unknown>(`/api/v1/admin/system/cache/dict/${dictType}`, { method: 'DELETE' })
    toast.add({ title: '字典缓存已清除', description: `字典类型: ${dictType}`, color: 'success' })
    dictTypeInput.value = ''
  } catch (e) {
    toast.add({ title: '清除失败', description: e instanceof Error ? e.message : '', color: 'error' })
  } finally {
    clearingDictCache.value = false
  }
}

async function clearAllDictCache() {
  clearingAllDictCache.value = true
  try {
    await $api<unknown>('/api/v1/admin/system/cache/dict/all', { method: 'DELETE' })
    toast.add({ title: '全部字典缓存已清除', color: 'success' })
    showClearAllDictConfirm.value = false
  } catch (e) {
    toast.add({ title: '清除失败', description: e instanceof Error ? e.message : '', color: 'error' })
  } finally {
    clearingAllDictCache.value = false
  }
}
</script>

<template>
  <UDashboardPanel id="system-cache">
    <template #header>
      <UDashboardNavbar title="缓存管理">
        <template #leading>
          <UDashboardSidebarCollapse />
        </template>
      </UDashboardNavbar>
    </template>

    <template #body>
      <div class="space-y-6 p-6">
        <!-- 用户缓存 -->
        <UCard>
          <template #header>
            <div class="flex items-center gap-2">
              <UIcon name="i-lucide-user-x" class="text-primary size-5" />
              <span class="font-semibold">用户缓存</span>
            </div>
          </template>

          <p class="mb-4 text-sm text-muted">
            清除指定用户的会话信息、权限缓存等。适用于用户角色变更后立即生效的场景。
          </p>

          <div class="flex items-center gap-3">
            <UInput
              v-model="userIdInput"
              placeholder="输入用户 ID"
              type="number"
              class="w-48"
              @keyup.enter="clearUserCache"
            />
            <UButton
              icon="i-lucide-trash-2"
              color="warning"
              :loading="clearingUserCache"
              @click="clearUserCache"
            >
              清除用户缓存
            </UButton>
          </div>
        </UCard>

        <!-- 字典缓存 -->
        <UCard>
          <template #header>
            <div class="flex items-center gap-2">
              <UIcon name="i-lucide-book-x" class="text-primary size-5" />
              <span class="font-semibold">字典缓存</span>
            </div>
          </template>

          <p class="mb-4 text-sm text-muted">
            清除字典数据缓存。修改字典项后，如需立即生效，可按字典类型清除缓存，或一键清除全部字典缓存。
          </p>

          <div class="space-y-4">
            <!-- 按类型清除 -->
            <div class="flex items-center gap-3">
              <UInput
                v-model="dictTypeInput"
                placeholder="输入字典类型（如 user_status）"
                class="w-64"
                @keyup.enter="clearDictCache"
              />
              <UButton
                icon="i-lucide-trash-2"
                color="warning"
                :loading="clearingDictCache"
                @click="clearDictCache"
              >
                清除指定字典缓存
              </UButton>
            </div>

            <!-- 清除全部 -->
            <div class="flex items-center gap-3">
              <UButton
                icon="i-lucide-bomb"
                color="error"
                variant="soft"
                @click="showClearAllDictConfirm = true"
              >
                清除全部字典缓存
              </UButton>
              <span class="text-xs text-muted">将清除所有字典类型的缓存数据</span>
            </div>
          </div>
        </UCard>
      </div>

      <!-- 清除全部字典缓存确认弹窗 -->
      <UModal v-model:open="showClearAllDictConfirm" title="确认清除全部字典缓存">
        <template #body>
          <p class="text-sm">
            此操作将清除
            <strong>全部字典类型</strong>
            的缓存数据，下次访问时将从数据库重新加载。确认继续？
          </p>
        </template>
        <template #footer>
          <div class="flex justify-end gap-2">
            <UButton
              color="neutral"
              variant="soft"
              @click="showClearAllDictConfirm = false"
            >
              取消
            </UButton>
            <UButton
              color="error"
              :loading="clearingAllDictCache"
              @click="clearAllDictCache"
            >
              确认清除
            </UButton>
          </div>
        </template>
      </UModal>
    </template>
  </UDashboardPanel>
</template>
