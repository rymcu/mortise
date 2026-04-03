<script setup lang="ts">
import type { PasswordFormProps } from '~/types/profile'

defineProps<PasswordFormProps>()

const emit = defineEmits<{
  'update:currentPassword': [value: string]
  'update:newPassword': [value: string]
  'update:confirmPassword': [value: string]
  'update:showCurrent': [value: boolean]
  'update:showNew': [value: boolean]
  'update:showConfirm': [value: boolean]
  submit: []
}>()
</script>

<template>
  <UCard>
    <template #header>
      <div class="flex flex-col gap-1 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <h2 class="text-lg font-semibold">修改密码</h2>
          <p class="text-muted mt-1 text-sm">使用更强的密码组合来提升账户安全性。</p>
        </div>
        <UBadge color="error" variant="soft">安全设置</UBadge>
      </div>
    </template>

    <div class="space-y-5">
      <UFormField label="当前密码" required>
        <UInput
          :model-value="currentPassword"
          :type="showCurrent ? 'text' : 'password'"
          placeholder="请输入当前密码"
          :ui="{ trailing: 'pe-1' }"
          class="w-full"
          @update:model-value="emit('update:currentPassword', $event as string)"
        >
          <template #trailing>
            <UButton
              color="neutral"
              variant="link"
              size="sm"
              :icon="showCurrent ? 'i-lucide-eye-off' : 'i-lucide-eye'"
              :aria-label="showCurrent ? '隐藏密码' : '显示密码'"
              :aria-pressed="showCurrent"
              @click="emit('update:showCurrent', !showCurrent)"
            />
          </template>
        </UInput>
      </UFormField>

      <UFormField label="新密码" required>
        <UInput
          :model-value="newPassword"
          :type="showNew ? 'text' : 'password'"
          :color="color"
          placeholder="请输入新密码"
          :ui="{ trailing: 'pe-1' }"
          :aria-invalid="score < 4"
          class="w-full"
          @update:model-value="emit('update:newPassword', $event as string)"
        >
          <template #trailing>
            <UButton
              color="neutral"
              variant="link"
              size="sm"
              :icon="showNew ? 'i-lucide-eye-off' : 'i-lucide-eye'"
              :aria-label="showNew ? '隐藏密码' : '显示密码'"
              :aria-pressed="showNew"
              @click="emit('update:showNew', !showNew)"
            />
          </template>
        </UInput>
      </UFormField>

      <UProgress :color="color" :indicator="strengthText" :model-value="score" :max="4" size="sm" />

      <div class="grid gap-5 lg:grid-cols-[minmax(0,1fr)_240px] lg:items-start">
        <ul class="space-y-1">
          <li
            v-for="(rule, i) in strength"
            :key="i"
            class="flex items-center gap-1"
            :class="rule.met ? 'text-success' : 'text-muted'"
          >
            <UIcon :name="rule.met ? 'i-lucide-circle-check' : 'i-lucide-circle-x'" class="size-4 shrink-0" />
            <span class="text-xs">{{ rule.text }}</span>
          </li>
        </ul>

        <UCard class="border-dashed">
          <div class="space-y-2 text-sm">
            <div class="font-medium text-highlighted">安全建议</div>
            <p class="text-muted leading-6">避免和其他站点复用密码。修改成功后系统会要求你重新登录。</p>
          </div>
        </UCard>
      </div>

      <UFormField label="确认新密码" required>
        <UInput
          :model-value="confirmPassword"
          :type="showConfirm ? 'text' : 'password'"
          :color="confirmPassword ? (confirmMatched ? 'success' : 'error') : 'neutral'"
          placeholder="请再次输入新密码"
          :ui="{ trailing: 'pe-1' }"
          :aria-invalid="confirmPassword.length > 0 && !confirmMatched"
          class="w-full"
          @update:model-value="emit('update:confirmPassword', $event as string)"
        >
          <template #trailing>
            <UButton
              color="neutral"
              variant="link"
              size="sm"
              :icon="showConfirm ? 'i-lucide-eye-off' : 'i-lucide-eye'"
              :aria-label="showConfirm ? '隐藏密码' : '显示密码'"
              :aria-pressed="showConfirm"
              @click="emit('update:showConfirm', !showConfirm)"
            />
          </template>
        </UInput>
      </UFormField>

      <div class="flex justify-end">
        <UButton
          color="error"
          variant="outline"
          :loading="loading"
          :disabled="!canChangePassword"
          @click="emit('submit')"
        >
          提交修改
        </UButton>
      </div>
    </div>

    <UAlert v-if="error" color="error" variant="soft" :title="error" class="mt-4" />
    <UAlert v-if="success" color="success" variant="soft" :title="success" class="mt-4" />
  </UCard>
</template>
