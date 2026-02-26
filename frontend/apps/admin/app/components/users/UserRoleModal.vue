<script setup lang="ts">
/**
 * 用户角色配置弹窗
 * 打开时加载所有角色列表和用户已绑定的角色，通过复选框选择后保存
 */
import {
  fetchAdminGet,
  fetchAdminPost,
  fetchAdminPage
} from '@mortise/core-sdk'

interface RoleItem {
  id: string | number
  label?: string
  permission?: string
  status?: number
}

const props = defineProps<{
  open: boolean
  user: Record<string, unknown>
}>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'success'): void
}>()

const { $api } = useNuxtApp()

const isOpen = computed({
  get: () => props.open,
  set: (v) => emit('update:open', v)
})

const loading = ref(false)
const rolesLoading = ref(false)
const errorMessage = ref('')
const allRoles = ref<RoleItem[]>([])
const checkedIds = ref<Set<string | number>>(new Set())

/** 加载所有角色 + 当前用户已绑定角色 */
async function loadData() {
  if (!props.user?.id) return
  rolesLoading.value = true
  errorMessage.value = ''
  try {
    // 并行加载：所有角色列表 + 用户已绑定角色
    const [pageResult, userRoles] = await Promise.all([
      fetchAdminPage<RoleItem>($api, '/api/v1/admin/roles', {
        pageNum: 1,
        pageSize: 999
      }),
      fetchAdminGet<RoleItem[]>(
        $api,
        `/api/v1/admin/users/${props.user.id}/roles`
      )
    ])
    allRoles.value = pageResult.records || []
    checkedIds.value = new Set((userRoles || []).map((r) => r.id))
  } catch (err) {
    errorMessage.value = err instanceof Error ? err.message : '加载角色失败'
  } finally {
    rolesLoading.value = false
  }
}

function toggleRole(id: string | number) {
  const s = new Set(checkedIds.value)
  if (s.has(id)) {
    s.delete(id)
  } else {
    s.add(id)
  }
  checkedIds.value = s
}

async function onSubmit() {
  if (!props.user?.id) return
  loading.value = true
  errorMessage.value = ''
  try {
    await fetchAdminPost($api, `/api/v1/admin/users/${props.user.id}/roles`, {
      idUser: String(props.user.id),
      idRoles: Array.from(checkedIds.value).map(String)
    })
    isOpen.value = false
    emit('success')
  } catch (err) {
    errorMessage.value = err instanceof Error ? err.message : '保存失败'
  } finally {
    loading.value = false
  }
}

watch(
  () => props.open,
  (val) => {
    if (val) loadData()
  }
)
</script>

<template>
  <UModal v-model:open="isOpen" title="配置角色">
    <template #body>
      <div class="space-y-4">
        <p class="text-muted text-sm">
          为用户「{{ user.nickname || user.account }}」分配角色
        </p>

        <UAlert
          v-if="errorMessage"
          color="error"
          variant="soft"
          :title="errorMessage"
        />

        <div v-if="rolesLoading" class="flex h-32 items-center justify-center">
          <span class="text-muted text-sm">加载角色列表中...</span>
        </div>

        <div v-else class="max-h-80 space-y-2 overflow-y-auto">
          <div
            v-for="role in allRoles"
            :key="String(role.id)"
            class="hover:bg-elevated/50 flex cursor-pointer items-center gap-3 rounded-lg px-3 py-2 transition-colors"
            @click="toggleRole(role.id)"
          >
            <input
              type="checkbox"
              :checked="checkedIds.has(role.id)"
              class="border-default rounded"
              @click.stop="toggleRole(role.id)"
            />
            <div class="flex-1">
              <span class="text-sm font-medium">{{ role.label }}</span>
              <span v-if="role.permission" class="text-muted ml-2 text-xs">{{
                role.permission
              }}</span>
            </div>
          </div>
          <p
            v-if="!allRoles.length"
            class="text-muted py-4 text-center text-sm"
          >
            暂无可用角色
          </p>
        </div>
      </div>
    </template>
    <template #footer>
      <div class="flex w-full justify-end gap-2">
        <UButton color="primary" :loading="loading" @click="onSubmit">
          保存
        </UButton>
        <UButton
          color="neutral"
          variant="subtle"
          :disabled="loading"
          @click="isOpen = false"
        >
          取消
        </UButton>
      </div>
    </template>
  </UModal>
</template>
