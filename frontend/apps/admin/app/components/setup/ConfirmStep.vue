<script setup lang="ts">
defineProps<{
  systemState: { systemName: string; systemDescription?: string }
  adminState: { adminNickname: string; adminEmail: string }
  isInitializing: boolean
  progress: number
  successMessage: string
  errorMessage: string
  loading: boolean
}>()

const emit = defineEmits<{
  prev: []
  initialize: []
}>()
</script>

<template>
  <div class="space-y-4">
    <div class="border-default space-y-3 rounded-lg border p-4">
      <h3
        class="flex items-center gap-2 font-medium text-gray-900 dark:text-white"
      >
        <UIcon name="i-lucide-settings" class="text-primary h-4 w-4" />
        系统信息
      </h3>
      <div class="grid grid-cols-[auto_1fr] gap-x-4 gap-y-1 text-sm">
        <span class="text-gray-500 dark:text-gray-400">系统名称</span>
        <span class="text-gray-900 dark:text-white">{{
          systemState.systemName
        }}</span>
        <span class="text-gray-500 dark:text-gray-400">系统描述</span>
        <span class="text-gray-900 dark:text-white">{{
          systemState.systemDescription || '—'
        }}</span>
      </div>
    </div>

    <div class="border-default space-y-3 rounded-lg border p-4">
      <h3
        class="flex items-center gap-2 font-medium text-gray-900 dark:text-white"
      >
        <UIcon name="i-lucide-user" class="text-primary h-4 w-4" />
        管理员信息
      </h3>
      <div class="grid grid-cols-[auto_1fr] gap-x-4 gap-y-1 text-sm">
        <span class="text-gray-500 dark:text-gray-400">昵称</span>
        <span class="text-gray-900 dark:text-white">{{
          adminState.adminNickname
        }}</span>
        <span class="text-gray-500 dark:text-gray-400">邮箱</span>
        <span class="text-gray-900 dark:text-white">{{
          adminState.adminEmail
        }}</span>
        <span class="text-gray-500 dark:text-gray-400">密码</span>
        <span class="text-gray-900 dark:text-white">••••••••</span>
      </div>
    </div>

    <!-- 初始化进度 -->
    <div v-if="isInitializing" class="space-y-2">
      <div class="flex items-center justify-between text-sm">
        <span class="text-gray-500 dark:text-gray-400">初始化进度</span>
        <span class="text-primary font-medium">{{ progress }}%</span>
      </div>
      <UProgress :value="progress" />
    </div>

    <UAlert
      v-if="successMessage"
      color="success"
      variant="soft"
      :title="successMessage"
      icon="i-lucide-circle-check"
    />

    <UAlert
      v-if="errorMessage"
      color="error"
      variant="soft"
      :title="errorMessage"
      icon="i-lucide-circle-x"
    />

    <div class="flex justify-between pt-2">
      <UButton
        color="neutral"
        variant="ghost"
        :disabled="isInitializing"
        @click="emit('prev')"
      >
        <template #leading>
          <UIcon name="i-lucide-arrow-left" />
        </template>
        上一步
      </UButton>
      <UButton
        :loading="loading"
        :disabled="!!successMessage"
        @click="emit('initialize')"
      >
        <template #leading>
          <UIcon name="i-lucide-rocket" />
        </template>
        开始初始化
      </UButton>
    </div>
  </div>
</template>
