<script setup lang="ts">
defineProps<{
  nickname: string
  gender: string
  birthDate: string
  genderItems: Array<{ label: string; value: string }>
  loading: boolean
  canSave: boolean
  hint: string
  success: string
  error: string
  displayUsername: string
  displayEmail: string
  displayPhone: string
}>()

const emit = defineEmits<{
  'update:nickname': [value: string]
  'update:gender': [value: string]
  'update:birthDate': [value: string]
  submit: []
}>()
</script>

<template>
  <UCard>
    <template #header>
      <div class="flex flex-col gap-1 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <h2 class="text-lg font-semibold">个人资料</h2>
          <p class="text-muted mt-1 text-sm">维护站点侧的基础身份信息，影响站内展示与账号识别。</p>
        </div>
        <UBadge color="neutral" variant="soft">基础档案</UBadge>
      </div>
    </template>

    <div class="space-y-6">
      <div class="grid grid-cols-1 gap-4 text-sm sm:grid-cols-2 xl:grid-cols-3">
        <div>
          <div class="text-muted">账号</div>
          <div class="font-medium">{{ displayUsername }}</div>
        </div>
        <div>
          <div class="text-muted">邮箱</div>
          <div class="font-medium">{{ displayEmail }}</div>
        </div>
        <div>
          <div class="text-muted">手机号</div>
          <div class="font-medium">{{ displayPhone }}</div>
        </div>
      </div>

      <USeparator />

      <div class="grid gap-4 lg:grid-cols-2">
        <UFormField label="昵称" required>
          <UInput
            :model-value="nickname"
            placeholder="请输入昵称"
            :disabled="loading"
            class="w-full"
            @update:model-value="emit('update:nickname', $event as string)"
          />
        </UFormField>

        <UFormField label="性别">
          <USelect
            :model-value="gender"
            :items="genderItems"
            value-key="value"
            label-key="label"
            :disabled="loading"
            class="w-full"
            @update:model-value="emit('update:gender', $event as string)"
          />
        </UFormField>

        <UFormField label="生日">
          <UInput
            :model-value="birthDate"
            type="date"
            :disabled="loading"
            class="w-full"
            @update:model-value="emit('update:birthDate', $event as string)"
          />
        </UFormField>

        <UCard class="border-dashed">
          <div class="space-y-2 text-sm">
            <div class="font-medium text-highlighted">维护提示</div>
            <p class="text-muted leading-6">昵称会直接影响评论、文章作者名等公开展示；头像变更后需要点击保存资料才会更新到站点资料。</p>
          </div>
        </UCard>
      </div>

      <UAlert v-if="hint" color="info" variant="soft" :title="hint" />

      <div class="flex justify-end">
        <UButton :loading="loading" :disabled="!canSave" @click="emit('submit')">
          保存资料
        </UButton>
      </div>
    </div>

    <UAlert v-if="success" color="success" variant="soft" :title="success" class="mt-4" />
    <UAlert v-if="error" color="error" variant="soft" :title="error" class="mt-4" />
  </UCard>
</template>
