<script setup lang="ts">
import type { ProfileNavItem, ProfileSection } from '../../types/profile'

defineProps<{
  displayAvatar: string | null
  displayName: string
  username: string
  activeSection: ProfileSection
  loading: boolean
  profileSummary: Array<{ label: string; value: string; icon: string }>
  pendingAvatar: string | null
  navItems: ProfileNavItem[]
}>()

const emit = defineEmits<{
  'update:activeSection': [value: ProfileSection]
  'avatar-change': [file: File]
  'avatar-clear': []
}>()

const avatarUploadFile = ref<File | null>(null)

function onFileChange(file: File | null | undefined) {
  if (!file) return
  emit('avatar-change', file)
  avatarUploadFile.value = null
}

function onClear(removeFile?: () => void) {
  avatarUploadFile.value = null
  removeFile?.()
  emit('avatar-clear')
}
</script>

<template>
  <aside class="space-y-4 xl:sticky xl:top-20">
    <UCard>
      <UFileUpload
        v-slot="{ open, removeFile }"
        v-model="avatarUploadFile"
        accept="image/*"
        :preview="false"
        :interactive="false"
        :reset="true"
        :disabled="loading"
        class="w-full"
        @update:model-value="onFileChange"
      >
        <div class="flex flex-col items-center gap-4 text-center">
          <UButton
            type="button"
            variant="ghost"
            color="neutral"
            style="width: 5.5rem; height: 5.5rem;"
            class="group relative shrink-0 overflow-hidden rounded-full p-0 ring-4 ring-primary/10"
            :disabled="loading"
            title="点击更换头像"
            @click="open()"
          >
            <img
              v-if="displayAvatar"
              :src="displayAvatar"
              alt="头像"
              style="width: 5.5rem; height: 5.5rem;"
              class="rounded-full object-cover"
            />
            <div
              v-else
              class="bg-primary/10 flex h-full w-full items-center justify-center"
            >
              <UIcon name="i-lucide-user" class="text-primary text-3xl" />
            </div>
            <div
              class="absolute inset-0 flex items-center justify-center rounded-full bg-black/40 opacity-0 transition-opacity group-hover:opacity-100"
            >
              <UIcon name="i-lucide-camera" class="text-xl text-white" />
            </div>
          </UButton>

          <div>
            <div class="font-semibold">{{ displayName }}</div>
            <div class="text-muted mt-1 text-xs">@{{ username }}</div>
          </div>

          <div class="flex flex-wrap items-center justify-center gap-2">
            <UButton color="neutral" variant="outline" size="sm" icon="i-lucide-upload" @click="open()">
              {{ pendingAvatar ? '重新选择头像' : '上传头像' }}
            </UButton>
            <UButton
              v-if="pendingAvatar"
              color="error"
              variant="link"
              size="sm"
              class="px-0"
              @click="onClear(removeFile)"
            >
              移除本次更改
            </UButton>
          </div>

          <div class="w-full space-y-2">
            <div
              v-for="item in profileSummary"
              :key="item.label"
              class="flex items-center justify-between gap-3 rounded-xl bg-elevated/60 px-3 py-2 text-sm"
            >
              <div class="flex items-center gap-2 text-muted">
                <UIcon :name="item.icon" class="size-4 shrink-0" />
                <span>{{ item.label }}</span>
              </div>
              <span class="max-w-[10rem] truncate text-right font-medium text-highlighted">{{ item.value }}</span>
            </div>
          </div>

          <p class="text-muted text-xs">点击头像后需保存资料才会正式生效。</p>
        </div>
      </UFileUpload>
    </UCard>

    <nav class="overflow-hidden rounded-2xl border border-default bg-default">
      <UButton
        v-for="item in navItems"
        :key="item.key"
        block
        color="neutral"
        variant="ghost"
        type="button"
        class="justify-start rounded-none px-4 py-4 text-left transition-colors"
        :class="activeSection === item.key
          ? 'bg-primary/10 text-primary'
          : 'text-default hover:bg-elevated'"
        @click="emit('update:activeSection', item.key)"
      >
        <UIcon :name="item.icon" class="mt-0.5 size-4 shrink-0" />
        <div>
          <div class="text-sm font-medium">{{ item.label }}</div>
          <div class="mt-1 text-xs" :class="activeSection === item.key ? 'text-primary/80' : 'text-muted'">
            {{ item.description }}
          </div>
        </div>
      </UButton>
    </nav>
  </aside>
</template>
