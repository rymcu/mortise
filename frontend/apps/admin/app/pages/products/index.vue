<script setup lang="ts">
import { fetchAdminGet, fetchAdminPage } from '@mortise/core-sdk'
import type { CategoryTree, ProductInfo } from '~/types/product'

const { $api } = useNuxtApp()

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
watch([productPageNum, productPageSize, productStatusFilter], () => loadProducts())

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
        <ProductsCategoryTreePanel
          :filtered-tree="filteredTree"
          :expanded-ids="expandedIds"
          :selected-category="selectedCategory"
          :tree-loading="treeLoading"
          :tree-error="treeError"
          :tree-keyword="treeKeyword"
          :product-total="productTotal"
          @update:tree-keyword="treeKeyword = $event"
          @toggle-expand="toggleExpand"
          @select-category="selectCategory"
          @clear-filter="clearCategoryFilter"
          @add-category="showCategoryAddModal = true"
          @edit-category="openEditCategory"
          @delete-category="openDeleteCategory"
          @reload="loadCategoryTree"
        />

        <ProductsListPanel
          :selected-category="selectedCategory"
          :product-records="productRecords"
          :product-columns="productColumns"
          :status-map="statusMap"
          :products-loading="productsLoading"
          :products-error="productsError"
          :product-status-filter="productStatusFilter"
          :product-status-options="productStatusOptions"
          :product-keyword="productKeyword"
          :product-total="productTotal"
          :product-page-num="productPageNum"
          :product-has-next="productHasNext"
          :product-has-previous="productHasPrevious"
          @update:product-status-filter="productStatusFilter = $event"
          @update:product-keyword="productKeyword = $event"
          @search="loadProducts"
          @add-product="showProductAddModal = true"
          @edit-product="openEditProduct"
          @sku-manage="(id) => navigateTo(`/products/${id}/skus`)"
          @status-product="openStatusProduct"
          @delete-product="openDeleteProduct"
          @refresh="loadProducts"
          @update:product-page-num="productPageNum = $event"
        />
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
