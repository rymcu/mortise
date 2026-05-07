<script setup lang="ts">
import { usePagedAdminResource } from '~/composables/usePagedAdminResource'
import type { AdminResourcePageProps } from '~/types/resource'

const props = withDefaults(
  defineProps<AdminResourcePageProps>(),
  {
    searchPlaceholder: '搜索关键字',
    emptyText: '暂无数据',
    errorMessage: '加载列表失败',
  },
)

const {
  loading,
  errorMessage,
  records,
  pageNum,
  pageSize,
  total,
  totalPage,
  hasNext,
  hasPrevious,
  keyword,
  load,
} = usePagedAdminResource<Record<string, unknown>>({
  path: props.path,
  errorMessage: props.errorMessage,
})

await load()
</script>

<template>
  <UDashboardPanel :id="panelId">
    <template #header>
      <UDashboardNavbar :title="title">
        <template #leading>
          <UDashboardSidebarCollapse />
        </template>
      </UDashboardNavbar>
    </template>

    <template #body>
      <div class="space-y-4 p-4">
        <slot name="intro" />

        <AdminPagedTableCard
          :columns="columns"
          :rows="records"
          :loading="loading"
          :error-message="errorMessage"
          :total="total"
          :page-num="pageNum"
          :page-size="pageSize"
          :total-page="totalPage"
          :has-next="hasNext"
          :has-previous="hasPrevious"
          :keyword="keyword"
          :search-placeholder="searchPlaceholder"
          :empty-text="emptyText"
          @update:keyword="keyword = $event"
          @update:page-num="pageNum = $event"
          @refresh="load"
          @search-enter="load"
        >
          <template #toolbar>
            <slot name="toolbar" />
          </template>

          <template
            v-for="column in columns"
            :key="column.key"
            #[`cell-${column.key}`]="slotProps"
          >
            <slot :name="`cell-${column.key}`" v-bind="slotProps">
              {{ slotProps.row[column.key] ?? '-' }}
            </slot>
          </template>
        </AdminPagedTableCard>
      </div>
    </template>
  </UDashboardPanel>
</template>
