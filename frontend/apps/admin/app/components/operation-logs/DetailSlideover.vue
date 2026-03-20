<script setup lang="ts">
import type { OperationLog } from '~/types'

defineProps<{
  open: boolean
  currentLog: OperationLog | null
}>()

defineEmits<{
  'update:open': [value: boolean]
}>()
</script>

<template>
  <USlideover :open="open" title="操作日志详情" @update:open="$emit('update:open', $event)">
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
</template>
