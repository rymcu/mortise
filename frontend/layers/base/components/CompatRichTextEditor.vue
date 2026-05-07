<script setup lang="ts">
import type {
  CompatRichTextEditorProps,
  RichTextEditorInstance,
  RichTextEditorToolbarGroup,
} from '../types/rich-text'
import type { CompatMediaPickerSelection } from '../types/media-picker'

const DEFAULT_TOOLBAR_ITEMS: RichTextEditorToolbarGroup[] = [
  [
    { kind: 'heading', level: 1, icon: 'i-lucide-heading-1', tooltip: { text: '标题 1' } },
    { kind: 'heading', level: 2, icon: 'i-lucide-heading-2', tooltip: { text: '标题 2' } },
    { kind: 'heading', level: 3, icon: 'i-lucide-heading-3', tooltip: { text: '标题 3' } },
    { kind: 'paragraph', icon: 'i-lucide-pilcrow', tooltip: { text: '正文' } },
  ],
  [
    { kind: 'mark', mark: 'bold', icon: 'i-lucide-bold', tooltip: { text: '粗体' } },
    { kind: 'mark', mark: 'italic', icon: 'i-lucide-italic', tooltip: { text: '斜体' } },
    { kind: 'mark', mark: 'strike', icon: 'i-lucide-strikethrough', tooltip: { text: '删除线' } },
    { kind: 'mark', mark: 'code', icon: 'i-lucide-code', tooltip: { text: '行内代码' } },
    { kind: 'link', icon: 'i-lucide-link', tooltip: { text: '插入链接' } },
  ],
  [
    { kind: 'bulletList', icon: 'i-lucide-list', tooltip: { text: '无序列表' } },
    { kind: 'orderedList', icon: 'i-lucide-list-ordered', tooltip: { text: '有序列表' } },
    { kind: 'blockquote', icon: 'i-lucide-quote', tooltip: { text: '引用' } },
    { kind: 'codeBlock', icon: 'i-lucide-square-code', tooltip: { text: '代码块' } },
    { kind: 'horizontalRule', icon: 'i-lucide-minus', tooltip: { text: '水平线' } },
  ],
  [
    { kind: 'undo', icon: 'i-lucide-undo-2', tooltip: { text: '撤销' } },
    { kind: 'redo', icon: 'i-lucide-redo-2', tooltip: { text: '重做' } },
    { kind: 'clearFormatting', icon: 'i-lucide-remove-formatting', tooltip: { text: '清除格式' } },
  ],
]

const content = defineModel<string>({ default: '' })
const mentionSearchTerm = defineModel<string>('mentionSearchTerm', { default: '' })

const props = withDefaults(
  defineProps<CompatRichTextEditorProps>(),
  {
    placeholder: '请输入内容',
    minHeightClass: 'min-h-48',
    contentType: 'html',
    toolbarButtonSize: 'sm',
    enableImageUpload: true,
    uploadEndpoint: '/api/v1/admin/files',
    uploadAccept: 'image/*',
    uploadMaxSize: 10 * 1024 * 1024,
    uploadFileKindLabel: '图片',
    resolveUploadedUrl: false,
    enableAssetPicker: true,
    assetPickerTitle: '插入媒体资产',
    assetPickerDescription: '图片会直接插入正文，其他媒体会以链接形式插入，便于继续编辑和发布。',
    showMentionMenu: false,
    mentionMenuItems: () => [],
    mentionMenuIgnoreFilter: false,
  }
)

const toast = useToast()
const { uploadFile } = useAppFileUpload()
const { resolveUrl } = useMediaUrl()

const toolbarItems = computed(() => props.toolbarItems ?? DEFAULT_TOOLBAR_ITEMS)

const imageInputRef = ref<HTMLInputElement | null>(null)
const imageUploading = ref(false)
const assetPickerOpen = ref(false)
const pendingEditor = shallowRef<RichTextEditorInstance | null>(null)
const canUseAssetPicker = computed(() => props.enableAssetPicker && Boolean(props.assetPickerProvider))

function isRecord(value: unknown): value is Record<string, unknown> {
  return !!value && typeof value === 'object' && !Array.isArray(value)
}

function toStringValue(value: unknown): string {
  if (typeof value === 'string') {
    return value
  }

  if (typeof value === 'number') {
    return String(value)
  }

  return ''
}

function getRecordString(record: Record<string, unknown>, keys: string[]): string {
  for (const key of keys) {
    const value = toStringValue(record[key])
    if (value.trim()) {
      return value
    }
  }

  return ''
}

function normalizeInsertUrl(url: string): string {
  if (!props.resolveUploadedUrl) {
    return url
  }

  return resolveUrl(url) || url
}

function clearPendingEditor() {
  pendingEditor.value = null
}

function openImagePicker(editor: RichTextEditorInstance) {
  if (imageUploading.value) {
    return
  }

  pendingEditor.value = editor
  imageInputRef.value?.click()
}

function openAssetPicker(editor: RichTextEditorInstance) {
  if (!canUseAssetPicker.value) {
    return
  }

  pendingEditor.value = editor
  assetPickerOpen.value = true
}

function canInsertAssetAsImage(selection: CompatMediaPickerSelection): boolean {
  if (selection.assetType === 'IMAGE') {
    return true
  }

  const fileType = toStringValue(selection.fileType).toLowerCase()
  if (fileType.startsWith('image/')) {
    return true
  }

  const extension = toStringValue(selection.fileExtension).toLowerCase()
  return ['png', 'jpg', 'jpeg', 'gif', 'webp', 'svg', 'bmp', 'ico'].includes(extension)
}

async function onEditorImageSelected(event: Event) {
  const input = event.target as HTMLInputElement | null
  const file = input?.files?.[0]
  const editor = pendingEditor.value

  if (!file || !editor) {
    if (input) {
      input.value = ''
    }
    clearPendingEditor()
    return
  }

  imageUploading.value = true
  try {
    const url = await uploadFile(file, {
      endpoint: props.uploadEndpoint,
      fallbackMessage: '图片上传失败',
      accept: props.uploadAccept,
      maxSize: props.uploadMaxSize,
      fileKindLabel: props.uploadFileKindLabel,
    })

    editor.chain().focus().setImage({ src: normalizeInsertUrl(url), alt: file.name }).run()
  } catch (error) {
    toast.add({
      title: '图片上传失败',
      description: error instanceof Error ? error.message : '请稍后重试',
      color: 'error',
    })
  } finally {
    imageUploading.value = false
    if (input) {
      input.value = ''
    }
    clearPendingEditor()
  }
}

function handleAssetSelect(selection: CompatMediaPickerSelection) {
  const editor = pendingEditor.value
  if (!editor) {
    return
  }

  const assetUrl = selection.url?.trim() || ''
  if (!assetUrl) {
    toast.add({
      title: '插入失败',
      description: '当前媒体资产没有可用地址，请先补齐文件地址后再试。',
      color: 'warning',
    })
    clearPendingEditor()
    return
  }

  const label = selection.label?.trim() || selection.id || '媒体资源'
  const insertedUrl = normalizeInsertUrl(assetUrl)

  if (canInsertAssetAsImage(selection)) {
    editor.chain().focus().setImage({ src: insertedUrl, alt: label }).run()
  } else {
    editor.chain().focus().insertContent({
      type: 'paragraph',
      content: [
        {
          type: 'text',
          text: label,
          marks: [
            {
              type: 'link',
              attrs: {
                href: insertedUrl,
                target: '_blank',
                rel: 'noreferrer noopener',
              },
            },
          ],
        },
      ],
    }).run()
  }

  clearPendingEditor()
}

watch(assetPickerOpen, (value) => {
  if (!value) {
    clearPendingEditor()
  }
})
</script>

<template>
  <div class="border-default focus-within:ring-primary/50 w-full overflow-hidden rounded-md border focus-within:ring-2">
    <UEditor
      v-model="content"
      :content-type="props.contentType"
      :placeholder="props.placeholder"
      :class="props.minHeightClass"
    >
      <template #default="{ editor }">
        <div class="border-default flex flex-wrap items-center gap-1 border-b px-2 py-1">
          <UEditorToolbar
            :editor="editor"
            :items="toolbarItems"
          />

          <UTooltip v-if="props.enableImageUpload" text="上传图片">
            <UButton
              type="button"
              color="neutral"
              variant="ghost"
              :size="props.toolbarButtonSize"
              icon="i-lucide-image-plus"
              :loading="imageUploading"
              aria-label="上传图片"
              @click="openImagePicker(editor)"
            />
          </UTooltip>

          <UTooltip v-if="canUseAssetPicker" text="从媒体台账插入">
            <UButton
              type="button"
              color="neutral"
              variant="ghost"
              :size="props.toolbarButtonSize"
              icon="i-lucide-library-big"
              aria-label="从媒体台账插入"
              @click="openAssetPicker(editor)"
            />
          </UTooltip>
        </div>

        <UEditorMentionMenu
          v-if="props.showMentionMenu"
          v-model:search-term="mentionSearchTerm"
          :editor="editor"
          :items="props.mentionMenuItems"
          :ignore-filter="props.mentionMenuIgnoreFilter"
        />
      </template>
    </UEditor>

    <input
      ref="imageInputRef"
      type="file"
      :accept="props.uploadAccept"
      class="hidden"
      @change="onEditorImageSelected"
    >
  </div>

  <CompatMediaPickerModal
    v-if="canUseAssetPicker && props.assetPickerProvider"
    v-model:open="assetPickerOpen"
    :provider="props.assetPickerProvider"
    :title="props.assetPickerTitle"
    :description="props.assetPickerDescription"
    :filter-values="props.assetPickerFilterValues"
    @select="handleAssetSelect"
  />
</template>
