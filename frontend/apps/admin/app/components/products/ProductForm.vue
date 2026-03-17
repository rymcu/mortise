<script setup lang="ts">
/**
 * 产品表单组件
 * 仅包含描述型元数据，不含定价/库存等交易属性
 */
import * as z from 'zod'
import { fetchAdminGet } from '@mortise/core-sdk'

interface CategoryOption {
  label: string
  value: string
}

interface ProductTypeItem {
  label: string
  value: string
}

const props = withDefaults(
  defineProps<{
    data?: Record<string, unknown>
  }>(),
  {
    data: () => ({})
  }
)

const emit = defineEmits<{
  (e: 'change', data: Record<string, unknown>): void
}>()

const { $api } = useNuxtApp()
const { resolveUrl } = useMediaUrl()
const toast = useToast()
const { uploadFile } = useAppFileUpload()

const schema = z.object({
  title: z.string().min(1, '请输入产品标题'),
  productType: z.string().min(1, '请选择产品类型'),
  categoryId: z.string().min(1, '请选择所属分类')
})

const state = reactive({
  title: '',
  subtitle: '',
  shortDescription: '',
  description: '',
  coverImageUrl: '',
  productType: '',
  categoryId: '' as string,
  seoTitle: '',
  seoDescription: '',
  seoKeywords: '',
  isFeatured: false,
  sortNo: 0,
  ...props.data
})

const formRef = ref()

// ─── 封面图片上传 ──────────────────────────────────────────────────────────────
const coverImageFile = ref<File | null>(null)
const coverImagePreview = ref<string | null>(null)
const coverUploading = ref(false)

const displayCoverUrl = computed(
  () => coverImagePreview.value || resolveUrl(state.coverImageUrl) || null
)

async function onCoverFileChange(file: File | null | undefined) {
  if (!file) return
  coverImagePreview.value = URL.createObjectURL(file)
  coverUploading.value = true
  try {
    state.coverImageUrl = await uploadFile(file, {
      endpoint: '/api/v1/admin/files',
      fallbackMessage: '封面上传失败',
      accept: 'image/*',
      maxSize: 10 * 1024 * 1024,
      fileKindLabel: '封面图片',
    })
    coverImagePreview.value = null
  } catch (e) {
    coverImagePreview.value = null
    toast.add({ title: '封面上传失败', description: e instanceof Error ? e.message : '请重试', color: 'error' })
  } finally {
    coverUploading.value = false
    coverImageFile.value = null
  }
}

function clearCoverImage() {
  coverImageFile.value = null
  state.coverImageUrl = ''
  coverImagePreview.value = null
}

// ─── 编辑器工具栏 ──────────────────────────────────────────────────────────────
const editorToolbarItems = [
  [
    { kind: 'heading', level: 1, icon: 'i-lucide-heading-1', tooltip: { text: '标题 1' } },
    { kind: 'heading', level: 2, icon: 'i-lucide-heading-2', tooltip: { text: '标题 2' } },
    { kind: 'heading', level: 3, icon: 'i-lucide-heading-3', tooltip: { text: '标题 3' } },
    { kind: 'paragraph', icon: 'i-lucide-pilcrow', tooltip: { text: '正文' } }
  ],
  [
    { kind: 'mark', mark: 'bold', icon: 'i-lucide-bold', tooltip: { text: '粗体' } },
    { kind: 'mark', mark: 'italic', icon: 'i-lucide-italic', tooltip: { text: '斜体' } },
    { kind: 'mark', mark: 'strike', icon: 'i-lucide-strikethrough', tooltip: { text: '删除线' } },
    { kind: 'mark', mark: 'code', icon: 'i-lucide-code', tooltip: { text: '行内代码' } }
  ],
  [
    { kind: 'bulletList', icon: 'i-lucide-list', tooltip: { text: '无序列表' } },
    { kind: 'orderedList', icon: 'i-lucide-list-ordered', tooltip: { text: '有序列表' } },
    { kind: 'blockquote', icon: 'i-lucide-quote', tooltip: { text: '引用' } },
    { kind: 'codeBlock', icon: 'i-lucide-square-code', tooltip: { text: '代码块' } },
    { kind: 'horizontalRule', icon: 'i-lucide-minus', tooltip: { text: '水平线' } }
  ],
  [
    { kind: 'undo', icon: 'i-lucide-undo-2', tooltip: { text: '撤销' } },
    { kind: 'redo', icon: 'i-lucide-redo-2', tooltip: { text: '重做' } },
    { kind: 'clearFormatting', icon: 'i-lucide-remove-formatting', tooltip: { text: '清除格式' } }
  ]
]

// 加载产品类型列表
const productTypeItems = ref<ProductTypeItem[]>([])
async function loadProductTypes() {
  try {
    const data = await fetchAdminGet<Record<string, string>>(
      $api,
      '/api/v1/admin/products/types'
    )
    productTypeItems.value = Object.entries(data || {}).map(([value, label]) => ({
      label: `${label} (${value})`,
      value
    }))
  } catch {
    // 静默失败
  }
}

// 加载分类列表（平铺展示）
const categoryOptions = ref<CategoryOption[]>([])

interface CategoryTree {
  id: string
  name: string
  parentId?: string | null
  children?: CategoryTree[]
}

async function loadCategories() {
  try {
    const tree = await fetchAdminGet<CategoryTree[]>(
      $api,
      '/api/v1/admin/product-categories/tree'
    )
    const flat: CategoryOption[] = []
    function flatten(nodes: CategoryTree[], depth = 0) {
      for (const node of nodes) {
        flat.push({
          label: `${'　'.repeat(depth)}${node.name}`,
          value: node.id
        })
        if (node.children?.length) {
          flatten(node.children, depth + 1)
        }
      }
    }
    flatten(tree || [])
    categoryOptions.value = flat
  } catch {
    // 静默失败
  }
}

onMounted(() => {
  loadProductTypes()
  loadCategories()
})

watch(state, (v) => emit('change', { ...v }), { deep: true })

async function validate(): Promise<boolean> {
  try {
    if (formRef.value?.validate) {
      await formRef.value.validate()
    }
    return schema.safeParse(state).success
  } catch {
    return false
  }
}

defineExpose({ validate, state })
</script>

<template>
  <UForm ref="formRef" :schema="schema" :state="state" class="space-y-4">
    <!-- 基本信息 -->
    <UFormField label="产品标题" name="title" required>
      <UInput v-model="state.title" placeholder="请输入产品标题" class="w-full" />
    </UFormField>

    <UFormField label="副标题" name="subtitle">
      <UInput
        v-model="state.subtitle"
        placeholder="请输入副标题（可选）"
        class="w-full"
      />
    </UFormField>

    <div class="grid grid-cols-2 gap-4">
      <UFormField label="产品类型" name="productType" required>
        <USelect
          v-model="state.productType"
          :items="productTypeItems"
          value-key="value"
          label-key="label"
          placeholder="请选择产品类型"
          class="w-full"
        />
      </UFormField>

      <UFormField label="所属分类" name="categoryId" required>
        <USelect
          v-model="state.categoryId"
          :items="categoryOptions"
          value-key="value"
          label-key="label"
          placeholder="请选择分类"
          class="w-full"
        />
      </UFormField>
    </div>

    <UFormField label="封面图片" name="coverImageUrl">
      <div v-if="displayCoverUrl" class="relative mb-2 w-fit">
        <img
          :src="displayCoverUrl"
          alt="封面预览"
          class="border-default h-32 rounded-md border object-cover"
        />
        <UButton
          icon="i-lucide-x"
          color="error"
          variant="soft"
          size="xs"
          class="absolute right-1 top-1"
          @click="clearCoverImage"
        />
      </div>

      <UFileUpload
        v-model="coverImageFile"
        accept="image/*"
        :disabled="coverUploading"
        :reset="true"
        :preview="false"
        color="neutral"
        variant="button"
        size="sm"
        icon="i-lucide-upload"
        :label="displayCoverUrl ? '更换封面' : '上传封面'"
        description="支持 PNG、JPG、WEBP 等常见图片格式"
        @update:model-value="onCoverFileChange"
      >
        <template #actions />
      </UFileUpload>
    </UFormField>

    <UFormField label="简短描述" name="shortDescription">
      <UTextarea
        v-model="state.shortDescription"
        placeholder="在产品列表中展示的简短描述"
        :rows="2"
        class="w-full"
      />
    </UFormField>

    <UFormField label="详细描述" name="description">
      <div class="border-default focus-within:ring-primary/50 w-full rounded-md border focus-within:ring-2">
        <UEditor
          v-model="state.description"
          content-type="html"
          placeholder="产品详细描述"
          class="min-h-48"
        >
          <template #default="{ editor }">
            <UEditorToolbar
              :editor="editor"
              :items="editorToolbarItems"
              class="border-default border-b"
            />
          </template>
        </UEditor>
      </div>
    </UFormField>

    <!-- SEO 信息 -->
    <div class="border-default rounded-lg border p-3 space-y-3">
      <p class="text-muted text-xs font-medium">SEO 信息（可选）</p>
      <UFormField label="SEO 标题" name="seoTitle">
        <UInput v-model="state.seoTitle" class="w-full" />
      </UFormField>
      <UFormField label="SEO 关键词" name="seoKeywords">
        <UInput
          v-model="state.seoKeywords"
          placeholder="多个关键词用逗号分隔"
          class="w-full"
        />
      </UFormField>
      <UFormField label="SEO 描述" name="seoDescription">
        <UTextarea v-model="state.seoDescription" :rows="2" class="w-full" />
      </UFormField>
    </div>

    <!-- 其他设置 -->
    <div class="grid grid-cols-2 gap-4">
      <UFormField label="排序号" name="sortNo">
        <UInput
          v-model.number="state.sortNo"
          type="number"
          placeholder="数字越小越靠前"
          class="w-full"
        />
      </UFormField>

      <UFormField label="是否推荐" name="isFeatured">
        <div class="pt-2">
          <UCheckbox v-model="state.isFeatured" label="推荐展示" />
        </div>
      </UFormField>
    </div>
  </UForm>
</template>
