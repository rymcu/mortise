<script setup lang="ts">
import type { ApiLog } from '~/types'

defineProps<{
  open: boolean
  currentLog: ApiLog | null
}>()

defineEmits<{
  'update:open': [value: boolean]
}>()

function statusColor(status?: number) {
  if (!status) return 'neutral'
  if (status >= 500) return 'error'
  if (status >= 400) return 'warning'
  if (status >= 200 && status < 300) return 'success'
  return 'neutral'
}
</script>

<template>
  <USlideover :open="open" title="API 日志详情" @update:open="$emit('update:open', $event)">
    <template #body>
      <div v-if="currentLog" class="space-y-3 p-4 text-sm">
        <div class="grid grid-cols-3 gap-x-4 gap-y-3">
          <div class="col-span-1 text-muted">日志ID</div>
          <div class="col-span-2 font-mono text-xs">{{ currentLog.id }}</div>

          <div class="col-span-1 text-muted">链路追踪ID</div>
          <div class="col-span-2 break-all font-mono text-xs">{{ currentLog.traceId || '-' }}</div>

          <div class="col-span-1 text-muted">客户端类型</div>
          <div class="col-span-2">{{ currentLog.clientType || '-' }}</div>

          <div class="col-span-1 text-muted">API 描述</div>
          <div class="col-span-2">{{ currentLog.apiDescription || '-' }}</div>

          <div class="col-span-1 text-muted">类方法</div>
          <div class="col-span-2 break-all font-mono text-xs">
            {{ currentLog.className ? `${currentLog.className}.${currentLog.methodName}` : '-' }}
          </div>

          <div class="col-span-1 text-muted">用户</div>
          <div class="col-span-2">{{ currentLog.username || '-' }}</div>

          <div class="col-span-1 text-muted">请求方式</div>
          <div class="col-span-2">
            <UBadge color="neutral" variant="outline" size="xs">{{ currentLog.requestMethod || '-' }}</UBadge>
          </div>

          <div class="col-span-1 text-muted">请求 URI</div>
          <div class="col-span-2 break-all font-mono text-xs">{{ currentLog.requestUri || '-' }}</div>

          <div class="col-span-1 text-muted">HTTP 状态码</div>
          <div class="col-span-2">
            <UBadge
              v-if="currentLog.httpStatus"
              :color="statusColor(currentLog.httpStatus)"
              variant="subtle"
              size="xs"
            >
              {{ currentLog.httpStatus }}
            </UBadge>
            <span v-else>-</span>
          </div>

          <div class="col-span-1 text-muted">客户端 IP</div>
          <div class="col-span-2">{{ currentLog.clientIp || '-' }}</div>

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

          <div class="col-span-1 text-muted">请求时间</div>
          <div class="col-span-2">{{ currentLog.requestTime || '-' }}</div>
        </div>

        <template v-if="currentLog.queryString">
          <div class="text-muted">查询参数</div>
          <pre class="overflow-x-auto rounded bg-elevated p-3 font-mono text-xs">{{ currentLog.queryString }}</pre>
        </template>

        <template v-if="currentLog.requestBody">
          <div class="text-muted">请求体</div>
          <pre class="overflow-x-auto rounded bg-elevated p-3 font-mono text-xs">{{ currentLog.requestBody }}</pre>
        </template>

        <template v-if="currentLog.responseBody">
          <div class="text-muted">响应体</div>
          <pre class="overflow-x-auto rounded bg-elevated p-3 font-mono text-xs">{{ currentLog.responseBody }}</pre>
        </template>

        <template v-if="currentLog.errorMsg">
          <div class="text-red-500">错误信息</div>
          <pre class="overflow-x-auto rounded bg-red-50 p-3 font-mono text-xs text-red-700 dark:bg-red-950 dark:text-red-300">{{ currentLog.errorMsg }}</pre>
        </template>
      </div>
    </template>
  </USlideover>
</template>
