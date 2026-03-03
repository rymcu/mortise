<script setup lang="ts">
import type { FormFieldDef } from '~/types'

/**
 * 通用动态表单字段渲染组件
 * <p>
 * 根据字段类型（FormFieldType）渲染对应的表单控件，支持所有后端定义的字段类型：
 * TEXT / EMAIL / NUMBER / PASSWORD / BOOLEAN / SELECT / IMAGE
 * <p>
 * IMAGE 类型内置图片上传逻辑：选文件后自动上传至 /api/v1/admin/files，
 * 上传成功后将文件 URL 通过 update:modelValue 事件传出。
 */
const props = defineProps<{
  field: FormFieldDef
  modelValue: string | undefined
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const { $api } = useNuxtApp()
const { resolveUrl } = useMediaUrl()
const toast = useToast()

// ─── PASSWORD 显示/隐藏 ────────────────────────────────────────────────────
const showPassword = ref(false)

// ─── IMAGE 上传 ───────────────────────────────────────────────────────────
const fileInputRef = ref<HTMLInputElement | null>(null)
const imagePreview = ref<string | null>(null)
const uploading = ref(false)

/** 当前展示的图片 URL（本地预览优先，其次已保存的 URL） */
const displayImageUrl = computed(
  () => imagePreview.value || resolveUrl(props.modelValue) || null
)

function triggerImageUpload() {
  fileInputRef.value?.click()
}

async function onImageFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  // 本地预览
  imagePreview.value = URL.createObjectURL(file)
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    const res = await $api<{ code: number; data: { url: string } }>(
      '/api/v1/admin/files',
      { method: 'POST', body: formData }
    )
    if (res?.data?.url) {
      emit('update:modelValue', res.data.url)
    } else {
      throw new Error('上传响应中缺少文件 URL')
    }
  } catch (e) {
    imagePreview.value = null
    toast.add({ title: '图片上传失败', description: e instanceof Error ? e.message : '请重试', color: 'error' })
  } finally {
    uploading.value = false
    input.value = ''
  }
}

function clearImage() {
  imagePreview.value = null
  emit('update:modelValue', '')
}
</script>

<template>
  <UFormField :label="field.label" :required="field.required">
    <!-- 布尔开关 -->
    <USwitch
      v-if="field.type === 'BOOLEAN'"
      :model-value="modelValue === 'true'"
      @update:model-value="$emit('update:modelValue', $event ? 'true' : 'false')"
    />

    <!-- 下拉选择 -->
    <USelect
      v-else-if="field.type === 'SELECT'"
      :model-value="modelValue"
      :items="field.options ?? []"
      value-key="value"
      class="w-full"
      @update:model-value="$emit('update:modelValue', $event as string)"
    />

    <!-- 密码 -->
    <UInput
      v-else-if="field.type === 'PASSWORD'"
      :model-value="modelValue"
      :type="showPassword ? 'text' : 'password'"
      :placeholder="field.placeholder"
      autocomplete="new-password"
      :ui="{ trailing: 'pe-1' }"
      class="w-full"
      @update:model-value="$emit('update:modelValue', $event)"
    >
      <template #trailing>
        <UButton
          color="neutral"
          variant="link"
          size="sm"
          :icon="showPassword ? 'i-lucide-eye-off' : 'i-lucide-eye'"
          :aria-label="showPassword ? '隐藏密码' : '显示密码'"
          :aria-pressed="showPassword"
          @click="showPassword = !showPassword"
        />
      </template>
    </UInput>

    <!-- 图片上传 -->
    <div
      v-else-if="field.type === 'IMAGE'"
      class="flex items-center gap-3"
    >
      <!-- 当前图片预览 -->
      <div
        v-if="displayImageUrl"
        class="relative size-16 shrink-0 overflow-hidden rounded-lg border border-default"
      >
        <img
          :src="displayImageUrl"
          :alt="field.label"
          class="size-full object-contain"
        />
        <button
          type="button"
          class="absolute right-0.5 top-0.5 flex size-4 items-center justify-center rounded-full bg-black/50 text-white hover:bg-black/70"
          aria-label="清除图片"
          @click="clearImage"
        >
          <UIcon name="i-lucide-x" class="size-3" />
        </button>
      </div>

      <!-- 未上传时的占位框 -->
      <div
        v-else
        class="flex size-16 shrink-0 items-center justify-center rounded-lg border border-dashed border-default bg-elevated/30"
      >
        <UIcon name="i-lucide-image" class="size-6 text-muted" />
      </div>

      <div class="flex flex-col gap-1.5">
        <UButton
          color="neutral"
          variant="outline"
          size="sm"
          icon="i-lucide-upload"
          :loading="uploading"
          @click="triggerImageUpload"
        >
          {{ displayImageUrl ? '重新上传' : '选择图片' }}
        </UButton>
        <p class="text-xs text-muted">支持 PNG、JPG、SVG、ICO 格式</p>
      </div>

      <!-- 隐藏的 file input -->
      <input
        ref="fileInputRef"
        type="file"
        accept="image/*"
        class="hidden"
        @change="onImageFileChange"
      />
    </div>

    <!-- TEXT / EMAIL / NUMBER（默认） -->
    <UInput
      v-else
      :model-value="modelValue"
      :type="field.type.toLowerCase()"
      :placeholder="field.placeholder"
      class="w-full"
      @update:model-value="$emit('update:modelValue', $event)"
    />
  </UFormField>
</template>
