<script setup lang="ts">
/**
 * 产品管理 - 主从视图
 * 左侧：产品分类树（可展开/折叠，点击节点过滤右侧产品列表）
 * 右侧：产品列表（含分页、搜索、CRUD 操作）
 */
import { fetchAdminGet, fetchAdminPage } from '@mortise/core-sdk'

interface CategoryTree {
  id: string
  name: string
  slug?: string
  description?: string
  status?: number
  isActive?: boolean
  sortNo?: number
  parentId?: string | null
  children?: CategoryTree[]
}

interface ProductInfo {
  id: string
  title?: string
  subtitle?: string
  productType?: string
  categoryId?: string
  status?: number
  isFeatured?: boolean
  sortNo?: number
  coverImageUrl?: string
  createdTime?: string
  publishedTime?: string
}

const { $api } = useNuxtApp()

// ============ 分类树（左侧） ============
const treeLoading = ref(false)
const treeError = ref('')
const categoryTree = ref<CategoryTree[]>([])
const expandedIds = ref<Set<string>>(new Set())
const selectedCategory = ref<CategoryTree | null>(null)
const treeKeyword = ref('')

async function loadCategoryTree() {
  treeLoading.value = true
  treeError.value = ''
  try {
    const tree = await fetchAdminGet<CategoryTree[]>(
      $api,
      '/api/v1/admin/product-categories/tree'
    )
    categoryTree.value = tree || []
    // 默认展开所有含子节点的一级分类
    const ids = new Set<string>()
    for (const node of categoryTree.value) {
      if (node.children?.length) ids.add(node.id)
    }
    expandedIds.value = ids
  } catch (err) {
    treeError.value = err instanceof Error ? err.message : '加载分类失败'
  } finally {
    treeLoading.value = false
  }
}

await loadCategoryTree()

function toggleExpand(id: string) {
  const s = new Set(expandedIds.value)
  if (s.has(id)) s.delete(id)
  else s.add(id)
  expandedIds.value = s
}

function selectCategory(cat: CategoryTree) {
  selectedCategory.value = cat
  productPageNum.value = 1
  productKeyword.value = ''
  loadProducts()
}

function clearCategoryFilter() {
  selectedCategory.value = null
  productPageNum.value = 1
  productKeyword.value = ''
  loadProducts()
}

/** 按关键字过滤分类树（保留匹配节点及其祖先） */
const filteredTree = computed(() => {
  if (!treeKeyword.value.trim()) return categoryTree.value
  const kw = treeKeyword.value.trim().toLowerCase()
  function filterNodes(nodes: CategoryTree[]): CategoryTree[] {
    const result: CategoryTree[] = []
    for (const node of nodes) {
      const childMatches = node.children?.length
        ? filterNodes(node.children)
        : []
      const selfMatch = (node.name || '').toLowerCase().includes(kw)
      if (selfMatch || childMatches.length > 0) {
        result.push({ ...node, children: childMatches.length ? childMatches : node.children })
      }
    }
    return result
  }
  return filterNodes(categoryTree.value)
})

// ============ 产品列表（右侧） ============
const productsLoading = ref(false)
const productsError = ref('')
const productRecords = ref<ProductInfo[]>([])
const productPageNum = ref(1)
const productPageSize = ref(10)
const productTotal = ref(0)
const productTotalPage = ref(0)
const productHasNext = ref(false)
const productHasPrevious = ref(false)
const productKeyword = ref('')
const productStatusFilter = ref<number>(-1) // -1 = 全部

async function loadProducts() {
  productsLoading.value = true
  productsError.value = ''
  try {
    const query: Record<string, unknown> = {
      pageNumber: productPageNum.value,
      pageSize: productPageSize.value
    }
    if (productKeyword.value) query.keyword = productKeyword.value
    if (selectedCategory.value) query.categoryId = selectedCategory.value.id
    if (productStatusFilter.value >= 0) query.status = productStatusFilter.value

    const page = await fetchAdminPage<ProductInfo>(
      $api,
      '/api/v1/admin/products',
      query
    )
    productRecords.value = page.records || []
    productTotal.value = page.totalRow || 0
    productTotalPage.value = page.totalPage || 0

    if (
      typeof page.pageNumber === 'number' &&
      page.pageNumber > 0 &&
      page.pageNumber !== productPageNum.value
    ) {
      productPageNum.value = page.pageNumber
    }
    productHasPrevious.value = Boolean(page.hasPrevious)
    productHasNext.value = Boolean(page.hasNext)
  } catch (err) {
    productsError.value = err instanceof Error ? err.message : '加载产品失败'
  } finally {
    productsLoading.value = false
  }
}

await loadProducts()

watch([productPageNum, productPageSize, productStatusFilter], () => {
  loadProducts()
})

// ============ 状态映射 ============
const statusMap: Record<number, { label: string; color: string }> = {
  0: { label: '草稿', color: 'neutral' },
  1: { label: '上架', color: 'success' },
  2: { label: '下架', color: 'warning' },
  3: { label: '停产', color: 'error' }
}

const productStatusOptions = [
  { label: '全部状态', value: -1 },
  { label: '草稿', value: 0 },
  { label: '上架', value: 1 },
  { label: '下架', value: 2 },
  { label: '停产', value: 3 }
]

// ============ 分类 CRUD 弹窗 ============
const showCategoryAddModal = ref(false)
const showCategoryEditModal = ref(false)
const showCategoryDeleteModal = ref(false)
const currentCategory = ref<Record<string, unknown>>({})

function openEditCategory(cat: CategoryTree) {
  currentCategory.value = { ...cat }
  showCategoryEditModal.value = true
}

function openDeleteCategory(cat: CategoryTree) {
  currentCategory.value = { ...cat }
  showCategoryDeleteModal.value = true
}

function onCategorySuccess() {
  loadCategoryTree()
  // 如果删除了当前选中的分类，重置右侧
  if (
    selectedCategory.value &&
    currentCategory.value?.id === selectedCategory.value.id
  ) {
    selectedCategory.value = null
    loadProducts()
  }
}

// ============ 产品 CRUD 弹窗 ============
const showProductAddModal = ref(false)
const showProductEditModal = ref(false)
const showProductDeleteModal = ref(false)
const showProductStatusModal = ref(false)
const currentProduct = ref<Record<string, unknown>>({})

function openEditProduct(row: Record<string, unknown>) {
  currentProduct.value = { ...row }
  showProductEditModal.value = true
}

function openDeleteProduct(row: Record<string, unknown>) {
  currentProduct.value = { ...row }
  showProductDeleteModal.value = true
}

function openStatusProduct(row: Record<string, unknown>) {
  currentProduct.value = { ...row }
  showProductStatusModal.value = true
}

// ============ 产品列表列定义 ============
const productColumns = [
  { key: 'title', label: '产品名称' },
  { key: 'productType', label: '类型' },
  { key: 'status', label: '状态' },
  { key: 'isFeatured', label: '推荐' },
  { key: 'sortNo', label: '排序' },
  { key: 'createdTime', label: '创建时间' }
]
</script>

<template>
  <UDashboardPanel id="products">
    <template #header>
      <UDashboardNavbar title="产品管理">
        <template #leading>
          <UDashboardSidebarCollapse />
        </template>
      </UDashboardNavbar>
    </template>

    <template #body>
      <div class="flex h-full gap-4 p-4">
        <!-- 左侧：产品分类树 -->
        <div class="w-72 shrink-0">
          <UCard class="h-full">
            <div class="mb-3 flex items-center justify-between gap-2">
              <h3 class="text-sm font-medium">产品分类</h3>
              <div class="flex items-center gap-1">
                <UButton
                  icon="i-lucide-plus"
                  color="primary"
                  variant="ghost"
                  size="xs"
                  @click="showCategoryAddModal = true"
                />
                <UButton
                  icon="i-lucide-refresh-cw"
                  color="neutral"
                  variant="ghost"
                  size="xs"
                  :loading="treeLoading"
                  @click="loadCategoryTree"
                />
              </div>
            </div>

            <UInput
              v-model="treeKeyword"
              placeholder="搜索分类名称"
              icon="i-lucide-search"
              size="sm"
              class="mb-2"
            />

            <UAlert
              v-if="treeError"
              color="error"
              variant="soft"
              :title="treeError"
              class="mb-2"
            />

            <!-- 全部产品入口 -->
            <div
              class="mb-1 flex cursor-pointer items-center gap-2 rounded-lg px-3 py-2 text-sm transition-colors"
              :class="
                !selectedCategory
                  ? 'bg-primary/10 text-primary ring-primary/20 ring-1'
                  : 'hover:bg-elevated/50'
              "
              @click="clearCategoryFilter"
            >
              <UIcon name="i-lucide-layout-grid" class="size-4 shrink-0" />
              <span class="font-medium">全部产品</span>
              <span class="text-muted ml-auto text-xs">{{
                productTotal
              }}</span>
            </div>

            <!-- 分类树节点 -->
            <div class="max-h-[65vh] overflow-y-auto">
              <template v-if="filteredTree.length">
                <div
                  v-for="node in filteredTree"
                  :key="node.id"
                >
                  <!-- 一级分类 -->
                  <div
                    class="group flex cursor-pointer items-center gap-1 rounded-lg px-2 py-1.5 text-sm transition-colors"
                    :class="
                      selectedCategory?.id === node.id
                        ? 'bg-primary/10 text-primary ring-primary/20 ring-1'
                        : 'hover:bg-elevated/50'
                    "
                    @click="selectCategory(node)"
                  >
                    <!-- 展开/折叠图标 -->
                    <UButton
                      v-if="node.children?.length"
                      :icon="
                        expandedIds.has(node.id)
                          ? 'i-lucide-chevron-down'
                          : 'i-lucide-chevron-right'
                      "
                      color="neutral"
                      variant="ghost"
                      size="xs"
                      class="shrink-0"
                      @click.stop="toggleExpand(node.id)"
                    />
                    <span v-else class="size-6 shrink-0" />

                    <div class="min-w-0 flex-1">
                      <div class="truncate font-medium">{{ node.name }}</div>
                    </div>

                    <!-- 操作按钮，hover 显示 -->
                    <div
                      class="ml-1 flex shrink-0 items-center gap-0.5 opacity-0 group-hover:opacity-100"
                    >
                      <UButton
                        icon="i-lucide-pencil"
                        color="neutral"
                        variant="ghost"
                        size="xs"
                        @click.stop="openEditCategory(node)"
                      />
                      <UButton
                        icon="i-lucide-trash-2"
                        color="error"
                        variant="ghost"
                        size="xs"
                        @click.stop="openDeleteCategory(node)"
                      />
                    </div>
                  </div>

                  <!-- 子分类 -->
                  <div
                    v-if="node.children?.length && expandedIds.has(node.id)"
                    class="ml-4"
                  >
                    <div
                      v-for="child in node.children"
                      :key="child.id"
                      class="group flex cursor-pointer items-center gap-1 rounded-lg px-2 py-1.5 text-sm transition-colors"
                      :class="
                        selectedCategory?.id === child.id
                          ? 'bg-primary/10 text-primary ring-primary/20 ring-1'
                          : 'hover:bg-elevated/50'
                      "
                      @click="selectCategory(child)"
                    >
                      <span class="size-6 shrink-0" />
                      <div class="min-w-0 flex-1">
                        <div class="text-muted truncate">{{ child.name }}</div>
                      </div>
                      <div
                        class="ml-1 flex shrink-0 items-center gap-0.5 opacity-0 group-hover:opacity-100"
                      >
                        <UButton
                          icon="i-lucide-pencil"
                          color="neutral"
                          variant="ghost"
                          size="xs"
                          @click.stop="openEditCategory(child)"
                        />
                        <UButton
                          icon="i-lucide-trash-2"
                          color="error"
                          variant="ghost"
                          size="xs"
                          @click.stop="openDeleteCategory(child)"
                        />
                      </div>
                    </div>
                  </div>
                </div>
              </template>
              <div
                v-else-if="!treeLoading"
                class="text-muted py-4 text-center text-sm"
              >
                暂无分类数据
              </div>
            </div>
          </UCard>
        </div>

        <!-- 右侧：产品列表 -->
        <div class="min-w-0 flex-1">
          <UCard class="h-full">
            <!-- 顶部：标题 + 工具栏 -->
            <div class="mb-4 flex items-center justify-between gap-2">
              <div>
                <h3 class="text-sm font-medium">
                  <template v-if="selectedCategory">
                    {{ selectedCategory.name }}
                    <span class="text-muted font-normal"
                      >（{{ selectedCategory.slug }}）</span
                    >
                  </template>
                  <template v-else>全部产品</template>
                </h3>
                <p
                  v-if="selectedCategory?.description"
                  class="text-muted mt-0.5 text-xs"
                >
                  {{ selectedCategory.description }}
                </p>
              </div>

              <div class="flex items-center gap-2">
                <!-- 状态筛选 -->
                <USelect
                  v-model="productStatusFilter"
                  :items="productStatusOptions"
                  value-key="value"
                  label-key="label"
                  class="w-32"
                />

                <!-- 关键字搜索 -->
                <UInput
                  v-model="productKeyword"
                  placeholder="搜索产品名称"
                  icon="i-lucide-search"
                  size="sm"
                  class="w-44"
                  @keyup.enter="loadProducts"
                />

                <UButton
                  icon="i-lucide-plus"
                  color="primary"
                  variant="soft"
                  size="sm"
                  @click="showProductAddModal = true"
                >
                  新增产品
                </UButton>
                <UButton
                  icon="i-lucide-refresh-cw"
                  color="neutral"
                  variant="soft"
                  size="sm"
                  :loading="productsLoading"
                  @click="loadProducts"
                >
                  刷新
                </UButton>
              </div>
            </div>

            <UAlert
              v-if="productsError"
              color="error"
              variant="soft"
              :title="productsError"
              class="mb-4"
            />

            <!-- 产品表格 -->
            <div class="overflow-x-auto">
              <table class="min-w-full text-sm">
                <thead>
                  <tr class="border-default border-b">
                    <th
                      v-for="col in productColumns"
                      :key="col.key"
                      class="whitespace-nowrap px-3 py-2 text-left font-medium"
                    >
                      {{ col.label }}
                    </th>
                    <th class="px-3 py-2 text-right font-medium">操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="product in productRecords"
                    :key="product.id"
                    class="border-default/60 hover:bg-elevated/50 border-b transition-colors"
                  >
                    <!-- 产品名称（含封面图） -->
                    <td class="px-3 py-2">
                      <div class="flex items-center gap-2">
                        <img
                          v-if="product.coverImageUrl"
                          :src="product.coverImageUrl"
                          class="size-8 shrink-0 rounded object-cover"
                          alt=""
                        />
                        <div
                          v-else
                          class="bg-elevated size-8 shrink-0 rounded flex items-center justify-center"
                        >
                          <UIcon
                            name="i-lucide-package"
                            class="text-muted size-4"
                          />
                        </div>
                        <div class="min-w-0">
                          <div class="truncate font-medium max-w-48">
                            {{ product.title || '-' }}
                          </div>
                        </div>
                      </div>
                    </td>

                    <!-- 类型 -->
                    <td class="text-muted px-3 py-2">
                      <UBadge color="neutral" variant="outline" size="sm">
                        {{ product.productType || '-' }}
                      </UBadge>
                    </td>

                    <!-- 状态 -->
                    <td class="px-3 py-2">
                      <UBadge
                        v-if="product.status !== undefined && product.status !== null"
                        :color="statusMap[product.status]?.color as any || 'neutral'"
                        variant="subtle"
                        size="sm"
                      >
                        {{ statusMap[product.status]?.label || '-' }}
                      </UBadge>
                      <span v-else class="text-muted">-</span>
                    </td>

                    <!-- 推荐 -->
                    <td class="px-3 py-2">
                      <UIcon
                        v-if="product.isFeatured"
                        name="i-lucide-star"
                        class="text-warning size-4"
                      />
                      <span v-else class="text-muted">-</span>
                    </td>

                    <!-- 排序 -->
                    <td class="text-muted px-3 py-2">
                      {{ product.sortNo ?? '-' }}
                    </td>

                    <!-- 创建时间 -->
                    <td class="text-muted whitespace-nowrap px-3 py-2">
                      {{
                        product.createdTime
                          ? product.createdTime.slice(0, 10)
                          : '-'
                      }}
                    </td>

                    <!-- 操作 -->
                    <td class="px-3 py-2 text-right">
                      <div class="flex items-center justify-end gap-1">
                        <UButton
                          icon="i-lucide-pencil"
                          color="neutral"
                          variant="ghost"
                          size="xs"
                          label="编辑"
                          @click="openEditProduct(product as Record<string, unknown>)"
                        />
                        <UButton
                          icon="i-lucide-layers"
                          color="neutral"
                          variant="ghost"
                          size="xs"
                          label="规格管理"
                          @click="navigateTo(`/products/${product.id}/skus`)"
                        />
                        <UButton
                          icon="i-lucide-toggle-left"
                          color="primary"
                          variant="ghost"
                          size="xs"
                            label="状态变更"
                          @click="openStatusProduct(product as Record<string, unknown>)"
                        />
                        <UButton
                          icon="i-lucide-trash-2"
                          color="error"
                          variant="ghost"
                          size="xs"
                            label="删除"
                          @click="openDeleteProduct(product as Record<string, unknown>)"
                        />
                      </div>
                    </td>
                  </tr>
                  <tr v-if="!productRecords.length && !productsLoading">
                    <td
                      :colspan="productColumns.length + 1"
                      class="text-muted py-8 text-center"
                    >
                      {{
                        selectedCategory
                          ? `「${selectedCategory.name}」下暂无产品`
                          : '暂无产品数据'
                      }}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <!-- 分页 -->
            <div
              class="text-muted mt-4 flex items-center justify-between text-sm"
            >
              <span>共 {{ productTotal }} 条</span>
              <div class="flex items-center gap-2">
                <UButton
                  color="neutral"
                  variant="ghost"
                  size="sm"
                  :disabled="!productHasPrevious"
                  @click="productPageNum--"
                >
                  上一页
                </UButton>
                <span>第 {{ productPageNum }} 页</span>
                <UButton
                  color="neutral"
                  variant="ghost"
                  size="sm"
                  :disabled="!productHasNext"
                  @click="productPageNum++"
                >
                  下一页
                </UButton>
              </div>
            </div>
          </UCard>
        </div>
      </div>

      <!-- 分类 CRUD 弹窗 -->
      <ProductCategoriesProductCategoryAddModal
        v-model:open="showCategoryAddModal"
        @success="onCategorySuccess"
      />
      <ProductCategoriesProductCategoryEditModal
        v-model:open="showCategoryEditModal"
        :category="currentCategory"
        @success="onCategorySuccess"
      />
      <ProductCategoriesProductCategoryDeleteModal
        v-model:open="showCategoryDeleteModal"
        :category="currentCategory"
        @success="onCategorySuccess"
      />

      <!-- 产品 CRUD 弹窗 -->
      <ProductsProductAddModal
        v-model:open="showProductAddModal"
        :default-category-id="selectedCategory?.id ?? null"
        @success="loadProducts"
      />
      <ProductsProductEditModal
        v-model:open="showProductEditModal"
        :product="currentProduct"
        @success="loadProducts"
      />
      <ProductsProductDeleteModal
        v-model:open="showProductDeleteModal"
        :product="currentProduct"
        @success="loadProducts"
      />
      <ProductsProductStatusModal
        v-model:open="showProductStatusModal"
        :product="currentProduct"
        @success="loadProducts"
      />
    </template>
  </UDashboardPanel>
</template>
