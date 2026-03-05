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
const coverFileInputRef = ref<HTMLInputElement | null>(null)
const coverImagePreview = ref<string | null>(null)
const coverUploading = ref(false)

const displayCoverUrl = computed(
  () => coverImagePreview.value || resolveUrl(state.coverImageUrl) || null
)

function triggerCoverUpload() {
  coverFileInputRef.value?.click()
}

async function onCoverFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  coverImagePreview.value = URL.createObjectURL(file)
  coverUploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    const res = await $api<{ code: number; data: { url: string } }>(
      '/api/v1/admin/files',
      { method: 'POST', body: formData }
    )
    if (res?.data?.url) {
      state.coverImageUrl = res.data.url
      coverImagePreview.value = null
    } else {
      throw new Error('上传响应中缺少文件 URL')
    }
  } catch (e) {
    coverImagePreview.value = null
    toast.add({ title: '封面上传失败', description: e instanceof Error ? e.message : '请重试', color: 'error' })
  } finally {
    coverUploading.value = false
    input.value = ''
  }
}

function clearCoverImage() {
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
        <select
          v-model="state.productType"
          class="border-default bg-default w-full rounded-md border px-3 py-2 text-sm"
        >
          <option value="">请选择产品类型</option>
          <option
            v-for="item in productTypeItems"
            :key="item.value"
            :value="item.value"
          >
            {{ item.label }}
          </option>
        </select>
      </UFormField>

      <UFormField label="所属分类" name="categoryId" required>
        <select
          v-model="state.categoryId"
          class="border-default bg-default w-full rounded-md border px-3 py-2 text-sm"
        >
          <option value="">请选择分类</option>
          <option
            v-for="opt in categoryOptions"
            :key="opt.value"
            :value="opt.value"
          >
            {{ opt.label }}
          </option>
        </select>
      </UFormField>
    </div>

    <UFormField label="封面图片" name="coverImageUrl">
      <!-- 隐藏文件输入 -->
      <input
        ref="coverFileInputRef"
        type="file"
        accept="image/*"
        class="hidden"
        @change="onCoverFileChange"
      />
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
      <UButton
        :icon="coverUploading ? 'i-lucide-loader-circle' : 'i-lucide-upload'"
        color="neutral"
        variant="outline"
        size="sm"
        :loading="coverUploading"
        @click="triggerCoverUpload"
      >
        {{ displayCoverUrl ? '更换封面' : '上传封面' }}
      </UButton>
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
        <div class="flex items-center gap-2 pt-2">
          <input
            v-model="state.isFeatured"
            type="checkbox"
            class="accent-primary"
          />
          <span class="text-sm">推荐展示</span>
        </div>
      </UFormField>
    </div>
  </UForm>
</template>
