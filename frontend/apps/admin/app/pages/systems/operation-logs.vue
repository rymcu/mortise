<script setup lang="ts">
import { fetchAdminPage } from '@mortise/core-sdk'
import type { OperationLog } from '~/types'

const { $api } = useNuxtApp()
const toast = useToast()

// 查询参数
const pageNum = ref(1)
const pageSize = ref(10)
const query = ref('')
const clientType = ref('')
const module = ref('')
const startDate = ref('')
const endDate = ref('')

// 数据状态
const loading = ref(false)
const records = ref<OperationLog[]>([])
const total = ref(0)
const totalPage = ref(0)
const hasNext = ref(false)
const hasPrevious = ref(false)
const errorMessage = ref('')

// 详情 Slideover
const showDetail = ref(false)
const currentLog = ref<OperationLog | null>(null)
// 删除确认
const showDeleteModal = ref(false)
const deleteTarget = ref<OperationLog | null>(null)
const deleting = ref(false)

const columns = [
  { key: 'operatorAccount', label: '操作人' },
  { key: 'module', label: '模块' },
  { key: 'operation', label: '操作' },
  { key: 'requestMethod', label: '方法' },
  { key: 'requestUri', label: '接口' },
  { key: 'clientType', label: '来源' },
  { key: 'duration', label: '耗时(ms)' },
  { key: 'success', label: '状态' },
  { key: 'operateTime', label: '操作时间' }
]

const clientTypeOptions = [
  { label: '全部', value: '' },
  { label: '后台管理', value: 'system' },
  { label: 'App 端', value: 'app' },
  { label: 'Web 端', value: 'web' },
  { label: '开放 API', value: 'api' }
]

async function loadLogs() {
  loading.value = true
  errorMessage.value = ''
  try {
    const page = await fetchAdminPage<OperationLog>($api, '/api/v1/admin/logs/operation', {
      pageNumber: pageNum.value,
      pageSize: pageSize.value,
      query: query.value || undefined,
      clientType: clientType.value || undefined,
      module: module.value || undefined,
      startDate: startDate.value || undefined,
      endDate: endDate.value || undefined
    })
    records.value = page.records || []
    total.value = page.totalRow || 0
    totalPage.value = page.totalPage || 0
    hasPrevious.value = Boolean(page.hasPrevious)
    hasNext.value = Boolean(page.hasNext)
  } catch (e) {
    errorMessage.value = e instanceof Error ? e.message : '加载操作日志失败'
  } finally {
    loading.value = false
  }
}

function openDetail(row: OperationLog) {
  currentLog.value = row
  showDetail.value = true
}

function openDeleteModal(row: OperationLog) {
  deleteTarget.value = row
  showDeleteModal.value = true
}

async function confirmDelete() {
  if (!deleteTarget.value) return
  deleting.value = true
  try {
    await $api<unknown>(`/api/v1/admin/logs/operation/${deleteTarget.value.id}`, { method: 'DELETE' })
    toast.add({ title: '删除成功', color: 'success' })
    showDeleteModal.value = false
    await loadLogs()
  } catch (e) {
    toast.add({ title: '删除失败', description: e instanceof Error ? e.message : '', color: 'error' })
  } finally {
    deleting.value = false
  }
}

watch([pageNum, pageSize], () => { loadLogs() })

await loadLogs()
</script>

<template>
  <UDashboardPanel id="system-operation-logs">
    <template #header>
      <UDashboardNavbar title="操作日志">
        <template #leading>
          <UDashboardSidebarCollapse />
        </template>
      </UDashboardNavbar>
    </template>

    <template #body>
      <UAlert
        v-if="errorMessage"
        color="error"
        variant="soft"
        :title="errorMessage"
        class="mb-4"
      />

      <UCard>
        <!-- 搜索工具栏 -->
        <div class="mb-4 flex flex-wrap items-center gap-2">
          <UInput
            v-model="query"
            placeholder="搜索操作人/接口/操作类型"
            icon="i-lucide-search"
            class="w-60"
            @keyup.enter="loadLogs"
          />
          <USelect
            v-model="clientType"
            :options="clientTypeOptions"
            value-attribute="value"
            option-attribute="label"
            class="w-36"
          />
          <UInput
            v-model="startDate"
            type="date"
            class="w-40"
            placeholder="开始日期"
          />
          <UInput
            v-model="endDate"
            type="date"
            class="w-40"
            placeholder="结束日期"
          />
          <UButton
            icon="i-lucide-search"
            @click="loadLogs"
          >
            查询
          </UButton>
          <UButton
            color="neutral"
            variant="soft"
            icon="i-lucide-refresh-cw"
            :loading="loading"
            @click="loadLogs"
          >
            刷新
          </UButton>
        </div>

        <!-- 数据表格 -->
        <div class="overflow-x-auto">
          <table class="min-w-full text-sm">
            <thead>
              <tr class="border-default border-b">
                <th
                  v-for="col in columns"
                  :key="col.key"
                  class="px-2 py-2 text-left font-medium"
                >
                  {{ col.label }}
                </th>
                <th class="px-2 py-2 text-right font-medium">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loading">
                <td :colspan="columns.length + 1" class="py-8 text-center text-muted">
                  加载中...
                </td>
              </tr>
              <tr v-else-if="records.length === 0">
                <td :colspan="columns.length + 1" class="py-8 text-center text-muted">
                  暂无操作日志
                </td>
              </tr>
              <tr
                v-for="row in records"
                :key="row.id"
                class="border-default border-b hover:bg-elevated/50 transition-colors"
              >
                <td class="px-2 py-2">{{ row.operatorAccount || '-' }}</td>
                <td class="px-2 py-2">{{ row.module || '-' }}</td>
                <td class="px-2 py-2">{{ row.operation || '-' }}</td>
                <td class="px-2 py-2">
                  <UBadge v-if="row.requestMethod" color="neutral" variant="outline" size="xs">
                    {{ row.requestMethod }}
                  </UBadge>
                  <span v-else>-</span>
                </td>
                <td class="max-w-xs truncate px-2 py-2 font-mono text-xs">{{ row.requestUri || '-' }}</td>
                <td class="px-2 py-2">
                  <UBadge color="neutral" variant="subtle" size="xs">{{ row.clientType || '-' }}</UBadge>
                </td>
                <td class="px-2 py-2">{{ row.duration != null ? row.duration : '-' }}</td>
                <td class="px-2 py-2">
                  <UBadge
                    :color="row.success === true ? 'success' : row.success === false ? 'error' : 'neutral'"
                    variant="subtle"
                    size="xs"
                  >
                    {{ row.success === true ? '成功' : row.success === false ? '失败' : '-' }}
                  </UBadge>
                </td>
                <td class="px-2 py-2 text-xs">{{ row.operateTime || '-' }}</td>
                <td class="px-2 py-2 text-right">
                  <div class="flex justify-end gap-1">
                    <UButton
                      icon="i-lucide-eye"
                      color="neutral"
                      variant="ghost"
                      size="xs"
                      @click="openDetail(row)"
                    />
                    <UButton
                      icon="i-lucide-trash-2"
                      color="error"
                      variant="ghost"
                      size="xs"
                      @click="openDeleteModal(row)"
                    />
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 分页 -->
        <div class="mt-4 flex items-center justify-between text-sm text-muted">
          <span>共 {{ total }} 条</span>
          <div class="flex items-center gap-2">
            <UButton
              size="xs"
              color="neutral"
              variant="soft"
              :disabled="!hasPrevious"
              icon="i-lucide-chevron-left"
              @click="pageNum--"
            />
            <span>{{ pageNum }} / {{ totalPage || 1 }}</span>
            <UButton
              size="xs"
              color="neutral"
              variant="soft"
              :disabled="!hasNext"
              icon="i-lucide-chevron-right"
              @click="pageNum++"
            />
          </div>
        </div>
      </UCard>

      <!-- 详情 Slideover -->
      <USlideover v-model:open="showDetail" title="操作日志详情">
        <template #body>
          <div v-if="currentLog" class="space-y-3 p-4 text-sm">
            <div class="grid grid-cols-3 gap-x-4 gap-y-3">
              <div class="col-span-1 text-muted">日志ID</div>
              <div class="col-span-2 font-mono text-xs">{{ currentLog.id }}</div>

              <div class="col-span-1 text-muted">链路追踪ID</div>
              <div class="col-span-2 break-all font-mono text-xs">{{ currentLog.traceId || '-' }}</div>

              <div class="col-span-1 text-muted">客户端类型</div>
              <div class="col-span-2">{{ currentLog.clientType || '-' }}</div>

              <div class="col-span-1 text-muted">模块</div>
              <div class="col-span-2">{{ currentLog.module || '-' }}</div>

              <div class="col-span-1 text-muted">操作</div>
              <div class="col-span-2">{{ currentLog.operation || '-' }}</div>

              <div class="col-span-1 text-muted">操作人</div>
              <div class="col-span-2">{{ currentLog.operatorAccount || '-' }}</div>

              <div class="col-span-1 text-muted">请求方式</div>
              <div class="col-span-2">
                <UBadge color="neutral" variant="outline" size="xs">{{ currentLog.requestMethod || '-' }}</UBadge>
              </div>

              <div class="col-span-1 text-muted">请求 URI</div>
              <div class="col-span-2 break-all font-mono text-xs">{{ currentLog.requestUri || '-' }}</div>

              <div class="col-span-1 text-muted">IP 地址</div>
              <div class="col-span-2">{{ currentLog.ip || '-' }}</div>

              <div class="col-span-1 text-muted">耗时（ms）</div>
              <div class="col-span-2">{{ currentLog.duration != null ? currentLog.duration : '-' }}</div>

              <div class="col-span-1 text-muted">状态</div>
              <div class="col-span-2">
                <UBadge
                  :color="currentLog.success === true ? 'success' : 'error'"
                  variant="subtle"
                  size="xs"
                >
                  {{ currentLog.success === true ? '成功' : '失败' }}
                </UBadge>
              </div>

              <div class="col-span-1 text-muted">操作时间</div>
              <div class="col-span-2">{{ currentLog.operateTime || '-' }}</div>
            </div>

            <template v-if="currentLog.params">
              <div class="text-muted">请求参数</div>
              <pre class="overflow-x-auto rounded bg-elevated p-3 font-mono text-xs">{{ currentLog.params }}</pre>
            </template>

            <template v-if="currentLog.result">
              <div class="text-muted">返回结果</div>
              <pre class="overflow-x-auto rounded bg-elevated p-3 font-mono text-xs">{{ currentLog.result }}</pre>
            </template>

            <template v-if="currentLog.errorMsg">
              <div class="text-muted text-red-500">错误信息</div>
              <pre class="overflow-x-auto rounded bg-red-50 p-3 font-mono text-xs text-red-700 dark:bg-red-950 dark:text-red-300">{{ currentLog.errorMsg }}</pre>
            </template>
          </div>
        </template>
      </USlideover>

      <!-- 删除确认 -->
      <UModal v-model:open="showDeleteModal" title="删除操作日志">
        <template #body>
          <p class="text-sm">确认删除此条操作日志记录吗？此操作不可恢复。</p>
        </template>
        <template #footer>
          <div class="flex justify-end gap-2">
            <UButton color="neutral" variant="soft" @click="showDeleteModal = false">取消</UButton>
            <UButton color="error" :loading="deleting" @click="confirmDelete">确认删除</UButton>
          </div>
        </template>
      </UModal>
    </template>
  </UDashboardPanel>
</template>
