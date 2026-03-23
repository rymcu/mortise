<script setup lang="ts">
import type { CategoryTree, ProductInfo } from '~/types/product'

defineProps<{
  selectedCategory: CategoryTree | null
  productRecords: ProductInfo[]
  productColumns: Array<{ key: string; label: string }>
  statusMap: Record<number, { label: string; color: string }>
  productsLoading: boolean
  productsError: string
  productStatusFilter: number
  productStatusOptions: Array<{ label: string; value: number }>
  productKeyword: string
  productTotal: number
  productPageNum: number
  productHasNext: boolean
  productHasPrevious: boolean
}>()

defineEmits<{
  'update:productStatusFilter': [value: number]
  'update:productKeyword': [value: string]
  'search': []
  'add-product': []
  'edit-product': [row: ProductInfo]
  'sku-manage': [id: string]
  'status-product': [row: ProductInfo]
  'delete-product': [row: ProductInfo]
  'refresh': []
  'update:productPageNum': [value: number]
}>()
</script>

<template>
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
            :model-value="productStatusFilter"
            :items="productStatusOptions"
            value-key="value"
            label-key="label"
            class="w-32"
            @update:model-value="$emit('update:productStatusFilter', $event as number)"
          />

          <!-- 关键字搜索 -->
          <UInput
            :model-value="productKeyword"
            placeholder="搜索产品名称"
            icon="i-lucide-search"
            size="sm"
            class="w-44"
            @update:model-value="$emit('update:productKeyword', $event as string)"
            @keyup.enter="$emit('search')"
          />

          <UButton
            icon="i-lucide-plus"
            color="primary"
            variant="soft"
            size="sm"
            @click="$emit('add-product')"
          >
            新增产品
          </UButton>
          <UButton
            icon="i-lucide-refresh-cw"
            color="neutral"
            variant="soft"
            size="sm"
            :loading="productsLoading"
            @click="$emit('refresh')"
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
                    @click="$emit('edit-product', product)"
                  />
                  <UButton
                    icon="i-lucide-layers"
                    color="neutral"
                    variant="ghost"
                    size="xs"
                    label="规格管理"
                    @click="$emit('sku-manage', product.id)"
                  />
                  <UButton
                    icon="i-lucide-toggle-left"
                    color="primary"
                    variant="ghost"
                    size="xs"
                    label="状态变更"
                    @click="$emit('status-product', product)"
                  />
                  <UButton
                    icon="i-lucide-trash-2"
                    color="error"
                    variant="ghost"
                    size="xs"
                    label="删除"
                    @click="$emit('delete-product', product)"
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
            @click="$emit('update:productPageNum', productPageNum - 1)"
          >
            上一页
          </UButton>
          <span>第 {{ productPageNum }} 页</span>
          <UButton
            color="neutral"
            variant="ghost"
            size="sm"
            :disabled="!productHasNext"
            @click="$emit('update:productPageNum', productPageNum + 1)"
          >
            下一页
          </UButton>
        </div>
      </div>
    </UCard>
  </div>
</template>
