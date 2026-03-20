<script setup lang="ts">
/**
 * 封面图片上传组件
 * 包含图片预览、上传按钮、清除按钮
 */
const props = defineProps<{
  imageUrl?: string | null
  uploading?: boolean
}>()

const emit = defineEmits<{
  (e: 'change', file: File): void
  (e: 'clear'): void
}>()

const { resolveUrl } = useMediaUrl()

const localFile = ref<File | null>(null)
const localPreview = ref<string | null>(null)

const displayUrl = computed(
  () => localPreview.value || resolveUrl(props.imageUrl) || null
)

// 服务端 URL 变化时（上传成功/外部清除），清除本地预览
watch(() => props.imageUrl, () => {
  localPreview.value = null
})

function onFileChange(file: File | null | undefined) {
  if (!file) return
  localPreview.value = URL.createObjectURL(file)
  emit('change', file)
}

function onClear() {
  localFile.value = null
  localPreview.value = null
  emit('clear')
}
</script>

<template>
  <UFormField label="封面图片" name="coverImageUrl">
    <div v-if="displayUrl" class="relative mb-2 w-fit">
      <img
        :src="displayUrl"
        alt="封面预览"
        class="border-default h-32 rounded-md border object-cover"
      />
      <UButton
        icon="i-lucide-x"
        color="error"
        variant="soft"
        size="xs"
        class="absolute right-1 top-1"
        @click="onClear"
      />
    </div>

    <UFileUpload
      v-model="localFile"
      accept="image/*"
      :disabled="uploading"
      :reset="true"
      :preview="false"
      color="neutral"
      variant="button"
      size="sm"
      icon="i-lucide-upload"
      :label="displayUrl ? '更换封面' : '上传封面'"
      description="支持 PNG、JPG、WEBP 等常见图片格式"
      @update:model-value="onFileChange"
    >
      <template #actions />
    </UFileUpload>
  </UFormField>
</template>
