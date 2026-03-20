<script setup lang="ts">
import type { InfoResponse } from '~/types'

defineProps<{
  info: InfoResponse | null
}>()
</script>

<template>
  <UCard>
    <template #header>
      <div class="flex items-center gap-2">
        <UIcon name="i-lucide-info" class="text-primary size-5" />
        <span class="font-semibold">应用信息</span>
      </div>
    </template>
    <template v-if="info">
      <div class="space-y-2 text-sm">
        <div
          v-if="info.app?.name"
          class="flex justify-between"
        >
          <span class="text-muted">应用名称</span>
          <span>{{ info.app.name }}</span>
        </div>
        <div
          v-if="info.app?.version"
          class="flex justify-between"
        >
          <span class="text-muted">版本</span>
          <span class="font-mono text-xs">{{ info.app.version }}</span>
        </div>
        <div
          v-if="info.java?.version"
          class="flex justify-between"
        >
          <span class="text-muted">Java 版本</span>
          <span class="font-mono text-xs">{{ info.java.version }}</span>
        </div>
        <template v-for="(val, key) in info" :key="key">
          <div
            v-if="key !== 'app' && key !== 'java' && typeof val !== 'object'"
            class="flex justify-between"
          >
            <span class="text-muted capitalize">{{ key }}</span>
            <span class="font-mono text-xs">{{ val }}</span>
          </div>
        </template>
      </div>
    </template>
    <div v-else class="text-center text-sm text-muted">
      暂无应用信息
    </div>
  </UCard>
</template>
