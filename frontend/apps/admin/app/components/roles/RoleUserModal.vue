<script setup lang="ts">
/**
 * 角色用户绑定弹窗
 * 打开时加载所有用户列表和角色已绑定的用户，通过复选框选择后保存
 */
import { fetchAdminGet, fetchAdminPut, fetchAdminPage } from '@mortise/core-sdk'

interface UserItem {
  id: string | number
  account?: string
  nickname?: string
  email?: string
  status?: number
}

const props = defineProps<{
  open: boolean
  role: Record<string, unknown>
}>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'success'): void
}>()

const { $api } = useNuxtApp()

const isOpen = computed({
  get: () => props.open,
  set: v => emit('update:open', v)
})

const loading = ref(false)
const dataLoading = ref(false)
const errorMessage = ref('')
const allUsers = ref<UserItem[]>([])
const checkedIds = ref<Set<string | number>>(new Set())

// 分页搜索
const searchKeyword = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

async function loadData() {
  if (!props.role?.id) return
  dataLoading.value = true
  errorMessage.value = ''
  try {
    const [pageResult, roleUsers] = await Promise.all([
      fetchAdminPage<UserItem>($api, '/api/v1/admin/users', {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        keyword: searchKeyword.value || undefined
      }),
      fetchAdminGet<UserItem[]>($api, `/api/v1/admin/roles/${props.role.id}/users`)
    ])
    allUsers.value = pageResult.records || []
    total.value = pageResult.totalRow || 0
    // 仅首次打开时初始化选中状态
    if (checkedIds.value.size === 0) {
      checkedIds.value = new Set((roleUsers || []).map(u => u.id))
    }
  } catch (err) {
    errorMessage.value = err instanceof Error ? err.message : '加载用户失败'
  } finally {
    dataLoading.value = false
  }
}

async function loadUsers() {
  dataLoading.value = true
  errorMessage.value = ''
  try {
    const pageResult = await fetchAdminPage<UserItem>($api, '/api/v1/admin/users', {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value || undefined
    })
    allUsers.value = pageResult.records || []
    total.value = pageResult.totalRow || 0
  } catch (err) {
    errorMessage.value = err instanceof Error ? err.message : '加载用户失败'
  } finally {
    dataLoading.value = false
  }
}

function toggleUser(id: string | number) {
  const s = new Set(checkedIds.value)
  if (s.has(id)) {
    s.delete(id)
  } else {
    s.add(id)
  }
  checkedIds.value = s
}

function handleSearch() {
  pageNum.value = 1
  loadUsers()
}

function prevPage() {
  if (pageNum.value > 1) {
    pageNum.value--
    loadUsers()
  }
}

function nextPage() {
  if (allUsers.value.length >= pageSize.value) {
    pageNum.value++
    loadUsers()
  }
}

async function onSubmit() {
  if (!props.role?.id) return
  loading.value = true
  errorMessage.value = ''
  try {
    await fetchAdminPut($api, `/api/v1/admin/roles/${props.role.id}/users`, {
      idRole: String(props.role.id),
      idUsers: Array.from(checkedIds.value).map(String)
    })
    isOpen.value = false
    emit('success')
  } catch (err) {
    errorMessage.value = err instanceof Error ? err.message : '保存失败'
  } finally {
    loading.value = false
  }
}

watch(() => props.open, (val) => {
  if (val) {
    checkedIds.value = new Set()
    pageNum.value = 1
    searchKeyword.value = ''
    loadData()
  }
})
</script>

<template>
  <UModal
    v-model:open="isOpen"
    title="配置用户"
    :ui="{ content: 'sm:max-w-2xl' }"
  >
    <template #body>
      <div class="space-y-4">
        <p class="text-sm text-muted">
          为角色「{{ role.label }}」分配用户
        </p>

        <UAlert
          v-if="errorMessage"
          color="error"
          variant="soft"
          :title="errorMessage"
        />

        <!-- 搜索栏 -->
        <div class="flex items-center gap-2">
          <UInput
            v-model="searchKeyword"
            placeholder="搜索账号/昵称"
            icon="i-lucide-search"
            class="flex-1"
            @keyup.enter="handleSearch"
          />
          <UButton
            color="neutral"
            variant="soft"
            :loading="dataLoading"
            @click="handleSearch"
          >
            搜索
          </UButton>
        </div>

        <!-- 用户列表 -->
        <div v-if="dataLoading && !allUsers.length" class="flex justify-center items-center h-32">
          <span class="text-sm text-muted">加载用户列表中...</span>
        </div>

        <div v-else class="max-h-80 overflow-y-auto space-y-1">
          <div
            v-for="user in allUsers"
            :key="String(user.id)"
            class="flex items-center gap-3 px-3 py-2 rounded-lg hover:bg-elevated/50 cursor-pointer transition-colors"
            @click="toggleUser(user.id)"
          >
            <input
              type="checkbox"
              :checked="checkedIds.has(user.id)"
              class="rounded border-default"
              @click.stop="toggleUser(user.id)"
            >
            <div class="flex-1 flex items-center gap-2">
              <span class="text-sm font-medium">{{ user.nickname || user.account }}</span>
              <span v-if="user.email" class="text-xs text-muted">{{ user.email }}</span>
            </div>
          </div>
          <p v-if="!allUsers.length" class="text-center text-sm text-muted py-4">
            暂无用户
          </p>
        </div>

        <!-- 分页 -->
        <div class="flex items-center justify-between text-sm text-muted">
          <span>已选 {{ checkedIds.size }} 人，共 {{ total }} 条</span>
          <div class="flex items-center gap-2">
            <UButton
              color="neutral"
              variant="ghost"
              size="xs"
              :disabled="pageNum <= 1"
              @click="prevPage"
            >
              上一页
            </UButton>
            <span>第 {{ pageNum }} 页</span>
            <UButton
              color="neutral"
              variant="ghost"
              size="xs"
              :disabled="allUsers.length < pageSize"
              @click="nextPage"
            >
              下一页
            </UButton>
          </div>
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
