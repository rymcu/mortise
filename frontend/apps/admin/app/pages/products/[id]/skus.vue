<script setup lang="ts">
/**
 * 产品 SKU 管理页面
 * 路由：/products/:id/skus
 * 管理产品规格单元（SKU）的增删改查，以及设置默认 SKU、切换状态
 */
import { fetchAdminGet } from '@mortise/core-sdk'
import type { ProductSkuInfo } from '~/types/migration'
import {
  isRecord,
  toOptionalBoolean,
  toOptionalString,
} from '~/utils/migration'

const route = useRoute()
const productId = route.params.id as string

const { $api } = useNuxtApp()

// ============ 产品信息（面包屑用） ============
const product = ref<Record<string, unknown>>({})

async function loadProduct() {
  try {
    const data = await fetchAdminGet<Record<string, unknown>>(
      $api,
      `/api/v1/admin/products/${productId}`
    )
    product.value = data || {}
  } catch {
    product.value = {}
  }
}

// ============ SKU 列表 ============
const loading = ref(false)
const error = ref('')
const skus = ref<ProductSkuInfo[]>([])

function normalizeSku(value: unknown): ProductSkuInfo | null {
  if (!isRecord(value)) {
    return null
  }

  const id = toOptionalString(value.id)
  if (!id) {
    return null
  }

  return {
    id,
    skuCode: toOptionalString(value.skuCode),
    name: toOptionalString(value.name),
    description: toOptionalString(value.description),
    status: toOptionalString(value.status),
    isDefault: toOptionalBoolean(value.isDefault),
    attributes: isRecord(value.attributes) ? value.attributes : undefined,
    createdTime: toOptionalString(value.createdTime)
  }
}

function normalizeSkus(value: unknown): ProductSkuInfo[] {
  if (!Array.isArray(value)) {
    return []
  }

  return value
    .map(item => normalizeSku(item))
    .filter((item): item is ProductSkuInfo => !!item)
}

async function loadSkus() {
  loading.value = true
  error.value = ''
  try {
    const list = await fetchAdminGet<unknown[]>(
      $api,
      `/api/v1/admin/products/${productId}/skus`
    )
    skus.value = normalizeSkus(list)
  } catch (err) {
    error.value = err instanceof Error ? err.message : '加载规格失败'
  } finally {
    loading.value = false
  }
}

await Promise.all([loadProduct(), loadSkus()])

// ============ 状态映射 ============
const skuStatusMap: Record<string, { label: string; color: string }> = {
  active: { label: '上架', color: 'success' },
  inactive: { label: '下架', color: 'warning' },
  discontinued: { label: '停售', color: 'error' }
}

// ============ CRUD 弹窗 ============
const showAddModal = ref(false)
const showEditModal = ref(false)
const showDeleteModal = ref(false)
const currentSku = ref<Record<string, unknown>>({})
const targetSku = ref<ProductSkuInfo | null>(null)
const showTargetSlideover = ref(false)

function openEdit(sku: Record<string, unknown>) {
  currentSku.value = { ...sku }
  showEditModal.value = true
}

function openDelete(sku: Record<string, unknown>) {
  currentSku.value = { ...sku }
  showDeleteModal.value = true
}

function openTargetManager(sku: ProductSkuInfo) {
  targetSku.value = { ...sku }
  showTargetSlideover.value = true
}

// ============ 设为默认 ============
const { patchAction, loading: _patchLoading } = useAdminCrud(
  `/api/v1/admin/products/${productId}/skus`
)
const settingDefaultId = ref<string | null>(null)

async function setDefault(sku: ProductSkuInfo) {
  settingDefaultId.value = sku.id
  const ok = await patchAction(`${sku.id}/default`)
  settingDefaultId.value = null
  if (ok !== null) loadSkus()
}

// ============ 切换状态 ============
const togglingStatusId = ref<string | null>(null)

async function toggleStatus(sku: ProductSkuInfo) {
  const current = sku.status as string
  const next = current === 'active' ? 'inactive' : 'active'
  togglingStatusId.value = sku.id
  const ok = await patchAction(`${sku.id}/status`, { status: next })
  togglingStatusId.value = null
  if (ok !== null) loadSkus()
}
</script>

<template>
  <UDashboardPanel id="product-skus">
    <template #header>
      <UDashboardNavbar :title="`规格管理 — ${product.title ?? productId}`">
        <template #leading>
          <UDashboardSidebarCollapse />
        </template>
        <template #right>
          <UButton
            icon="i-lucide-arrow-left"
            color="neutral"
            variant="ghost"
            size="sm"
            @click="navigateTo('/products')"
          >
            返回产品列表
          </UButton>
        </template>
      </UDashboardNavbar>
    </template>

    <template #body>
      <div class="p-4">
        <UCard>
          <!-- 顶部工具栏 -->
          <div class="mb-4 flex items-center justify-between gap-2">
            <div>
              <h3 class="text-sm font-medium">规格列表（SKU）</h3>
              <p class="text-muted mt-0.5 text-xs">
                管理产品「{{ product.title ?? productId }}」的所有规格单元
              </p>
            </div>
            <div class="flex items-center gap-2">
              <UButton
                icon="i-lucide-plus"
                color="primary"
                variant="soft"
                size="sm"
                @click="showAddModal = true"
              >
                新增规格
              </UButton>
              <UButton
                icon="i-lucide-refresh-cw"
                color="neutral"
                variant="soft"
                size="sm"
                :loading="loading"
                @click="loadSkus"
              >
                刷新
              </UButton>
            </div>
          </div>

          <UAlert
            v-if="error"
            color="error"
            variant="soft"
            :title="error"
            class="mb-4"
          />

          <!-- SKU 表格 -->
          <div class="overflow-x-auto">
            <table class="min-w-full text-sm">
              <thead>
                <tr class="border-default border-b">
                  <th class="px-3 py-2 text-left font-medium">编码</th>
                  <th class="px-3 py-2 text-left font-medium">名称</th>
                  <th class="px-3 py-2 text-left font-medium">规格属性</th>
                  <th class="px-3 py-2 text-left font-medium">状态</th>
                  <th class="px-3 py-2 text-left font-medium">默认</th>
                  <th class="px-3 py-2 text-left font-medium">创建时间</th>
                  <th class="px-3 py-2 text-right font-medium">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="sku in skus"
                  :key="String(sku.id)"
                  class="border-default/60 hover:bg-elevated/50 border-b transition-colors"
                >
                  <!-- 编码 -->
                  <td class="px-3 py-2">
                    <code class="bg-elevated rounded px-1.5 py-0.5 text-xs">
                      {{ sku.skuCode }}
                    </code>
                  </td>

                  <!-- 名称 -->
                  <td class="px-3 py-2 font-medium">{{ sku.name }}</td>

                  <!-- 规格属性 -->
                  <td class="px-3 py-2">
                    <div v-if="sku.attributes && Object.keys(sku.attributes).length" class="flex flex-wrap gap-1">
                      <UBadge
                        v-for="[k, v] in Object.entries(sku.attributes)"
                        :key="k"
                        color="neutral"
                        variant="soft"
                        size="sm"
                      >
                        {{ k }}: {{ v }}
                      </UBadge>
                    </div>
                    <span v-else class="text-muted">-</span>
                  </td>

                  <!-- 状态 -->
                  <td class="px-3 py-2">
                    <UBadge
                      :color="(skuStatusMap[String(sku.status)]?.color ?? 'neutral') as any"
                      variant="subtle"
                      size="sm"
                    >
                      {{ skuStatusMap[String(sku.status)]?.label ?? sku.status }}
                    </UBadge>
                  </td>

                  <!-- 默认 -->
                  <td class="px-3 py-2">
                    <UIcon
                      v-if="sku.isDefault"
                      name="i-lucide-star"
                      class="text-warning size-4"
                    />
                    <span v-else class="text-muted">-</span>
                  </td>

                  <!-- 创建时间 -->
                  <td class="text-muted whitespace-nowrap px-3 py-2">
                    {{ sku.createdTime ? String(sku.createdTime).slice(0, 10) : '-' }}
                  </td>

                  <!-- 操作 -->
                  <td class="px-3 py-2 text-right">
                    <div class="flex items-center justify-end gap-1">
                      <!-- 编辑 -->
                      <UButton
                        icon="i-lucide-pencil"
                        color="neutral"
                        variant="ghost"
                        size="xs"
                        @click="openEdit(sku as unknown as Record<string, unknown>)"
                      />
                      <UButton
                        icon="i-lucide-link-2"
                        color="primary"
                        variant="ghost"
                        size="xs"
                        title="目标映射"
                        @click="openTargetManager(sku)"
                      />
                      <!-- 设为默认 -->
                      <UButton
                        v-if="!sku.isDefault"
                        icon="i-lucide-star"
                        color="warning"
                        variant="ghost"
                        size="xs"
                        :loading="settingDefaultId === sku.id"
                        title="设为默认"
                        @click="setDefault(sku)"
                      />
                      <!-- 切换上架/下架 -->
                      <UButton
                        :icon="sku.status === 'active' ? 'i-lucide-toggle-right' : 'i-lucide-toggle-left'"
                        :color="sku.status === 'active' ? 'success' : 'neutral'"
                        variant="ghost"
                        size="xs"
                        :loading="togglingStatusId === sku.id"
                        :title="sku.status === 'active' ? '点击下架' : '点击上架'"
                        @click="toggleStatus(sku)"
                      />
                      <!-- 删除 -->
                      <UButton
                        icon="i-lucide-trash-2"
                        color="error"
                        variant="ghost"
                        size="xs"
                        @click="openDelete(sku as unknown as Record<string, unknown>)"
                      />
                    </div>
                  </td>
                </tr>
                <tr v-if="!skus.length && !loading">
                  <td colspan="7" class="text-muted py-8 text-center">
                    暂无规格数据，点击「新增规格」添加
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- 加载中 -->
          <div v-if="loading" class="text-muted py-6 text-center text-sm">
            加载中...
          </div>
        </UCard>
      </div>

      <!-- 新增 SKU -->
      <ProductSkusProductSkuAddModal
        v-model:open="showAddModal"
        :product-id="productId"
        @success="loadSkus"
      />

      <!-- 编辑 SKU -->
      <ProductSkusProductSkuEditModal
        v-model:open="showEditModal"
        :product-id="productId"
        :sku="currentSku"
        @success="loadSkus"
      />

      <!-- 删除 SKU -->
      <ProductSkusProductSkuDeleteModal
        v-model:open="showDeleteModal"
        :product-id="productId"
        :sku="currentSku"
        @success="loadSkus"
      />

      <ProductSkuTargetsTargetManagerSlideover
        v-model:open="showTargetSlideover"
        :product-id="productId"
        :sku="targetSku"
      />
    </template>
  </UDashboardPanel>
</template>
