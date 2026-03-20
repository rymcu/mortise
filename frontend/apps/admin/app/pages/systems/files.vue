<script setup lang="ts">
import { usePagedAdminResource } from '~/composables/usePagedAdminResource'
import type { FileDetail } from '~/types'

const { $api } = useNuxtApp()
const toast = useToast()

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
  load: loadFiles
} = usePagedAdminResource<FileDetail>({
  path: '/api/v1/admin/files',
  errorMessage: '加载文件列表失败'
})

const columns = [
  { key: 'preview', label: '预览' },
  { key: 'originalFilename', label: '文件名' },
  { key: 'contentType', label: '类型' },
  { key: 'size', label: '大小' },
  { key: 'platform', label: '存储平台' },
  { key: 'createTime', label: '上传时间' }
]

// 删除确认
const showDeleteModal = ref(false)
const deleteTarget = ref<FileDetail | null>(null)
const deleting = ref(false)

// 预览 Slideover
const showPreview = ref(false)
const previewFile = ref<FileDetail | null>(null)

function asFile(row: Record<string, unknown>): FileDetail {
  return row as unknown as FileDetail
}

function isImage(file: FileDetail) {
  return file.contentType?.startsWith('image/')
}

function formatSize(bytes?: number): string {
  if (!bytes) return '-'
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  if (bytes < 1024 * 1024 * 1024) return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
  return `${(bytes / (1024 * 1024 * 1024)).toFixed(2)} GB`
}

function openPreview(row: Record<string, unknown>) {
  previewFile.value = asFile(row)
  showPreview.value = true
}

function openDeleteModal(row: Record<string, unknown>) {
  deleteTarget.value = asFile(row)
  showDeleteModal.value = true
}

async function confirmDelete() {
  if (!deleteTarget.value) return
  deleting.value = true
  try {
    await $api(`/api/v1/admin/files/${deleteTarget.value.id}`, { method: 'DELETE' })
    toast.add({ title: '删除成功', color: 'success' })
    showDeleteModal.value = false
    deleteTarget.value = null
    await loadFiles()
  } catch (e: unknown) {
    const message = e instanceof Error ? e.message : '请稍后重试'
    toast.add({ title: '删除失败', description: message, color: 'error' })
  } finally {
    deleting.value = false
  }
}

await loadFiles()
</script>

<template>
  <UDashboardPanel id="system-files">
    <template #header>
      <UDashboardNavbar title="文件管理">
        <template #leading>
          <UDashboardSidebarCollapse />
        </template>
      </UDashboardNavbar>
    </template>

    <template #body>
      <AdminPagedTableCard
        :columns="columns"
        :rows="records as unknown as Record<string, unknown>[]"
        :loading="loading"
        :error-message="errorMessage"
        :total="total"
        :page-num="pageNum"
        :page-size="pageSize"
        :total-page="totalPage"
        :has-next="hasNext"
        :has-previous="hasPrevious"
        :keyword="keyword"
        show-actions
        search-placeholder="按文件名搜索"
        empty-text="暂无文件数据"
        @update:keyword="keyword = $event"
        @update:page-num="pageNum = $event"
        @refresh="loadFiles"
        @search-enter="loadFiles"
      >
        <!-- 缩略图预览 -->
        <template #cell-preview="{ row }">
          <div class="w-12 h-12 flex items-center justify-center">
            <img
              v-if="isImage(asFile(row)) && (asFile(row).thUrl || asFile(row).url)"
              :src="asFile(row).thUrl || asFile(row).url"
              alt="缩略图"
              class="w-12 h-12 object-cover rounded cursor-pointer"
              @click="openPreview(row)"
            />
            <UIcon
              v-else
              name="i-lucide-file"
              class="w-8 h-8 text-gray-400"
            />
          </div>
        </template>

        <!-- 文件名 -->
        <template #cell-originalFilename="{ row }">
          <span class="max-w-xs truncate block" :title="String(row.originalFilename ?? '')">
            {{ row.originalFilename || row.filename || '-' }}
          </span>
        </template>

        <!-- 内容类型 -->
        <template #cell-contentType="{ row }">
          <UBadge variant="soft" color="neutral" class="text-xs">
            {{ row.contentType || '-' }}
          </UBadge>
        </template>

        <!-- 文件大小 -->
        <template #cell-size="{ row }">
          {{ formatSize(row.size as number | undefined) }}
        </template>

        <!-- 存储平台 -->
        <template #cell-platform="{ row }">
          <UBadge variant="outline" color="primary">
            {{ row.platform || '-' }}
          </UBadge>
        </template>

        <!-- 上传时间 -->
        <template #cell-createTime="{ row }">
          {{ row.createTime ? String(row.createTime).replace('T', ' ').substring(0, 19) : '-' }}
        </template>

        <!-- 操作 -->
        <template #actions="{ row }">
          <UTooltip text="查看详情">
            <UButton
              variant="ghost"
              icon="i-lucide-eye"
              size="xs"
              @click="openPreview(row)"
            />
          </UTooltip>
          <UTooltip text="在新标签打开">
            <UButton
              variant="ghost"
              icon="i-lucide-external-link"
              size="xs"
              as="a"
              :href="String(row.url ?? '')"
              target="_blank"
            />
          </UTooltip>
          <UTooltip text="删除">
            <UButton
              variant="ghost"
              icon="i-lucide-trash-2"
              size="xs"
              color="error"
              @click="openDeleteModal(row)"
            />
          </UTooltip>
        </template>
      </AdminPagedTableCard>

      <!-- 文件详情 / 预览 Slideover -->
      <USlideover v-model:open="showPreview" :title="previewFile?.originalFilename ?? '文件详情'">
        <template #body>
          <div v-if="previewFile" class="p-4 space-y-4">
            <!-- 图片预览 -->
            <div v-if="isImage(previewFile)" class="flex justify-center">
              <img
                :src="previewFile.url"
                :alt="previewFile.originalFilename"
                class="max-w-full max-h-80 object-contain rounded border"
              />
            </div>

            <!-- 基本信息 -->
            <div class="divide-y divide-gray-100 dark:divide-gray-800 text-sm">
              <div class="flex py-2 gap-4">
                <span class="w-28 text-gray-500 shrink-0">文件 ID</span>
                <span class="font-mono">{{ previewFile.id }}</span>
              </div>
              <div class="flex py-2 gap-4">
                <span class="w-28 text-gray-500 shrink-0">原始文件名</span>
                <span class="break-all">{{ previewFile.originalFilename || '-' }}</span>
              </div>
              <div class="flex py-2 gap-4">
                <span class="w-28 text-gray-500 shrink-0">存储文件名</span>
                <span class="break-all font-mono text-xs">{{ previewFile.filename || '-' }}</span>
              </div>
              <div class="flex py-2 gap-4">
                <span class="w-28 text-gray-500 shrink-0">文件大小</span>
                <span>{{ formatSize(previewFile.size) }}</span>
              </div>
              <div class="flex py-2 gap-4">
                <span class="w-28 text-gray-500 shrink-0">内容类型</span>
                <span>{{ previewFile.contentType || '-' }}</span>
              </div>
              <div class="flex py-2 gap-4">
                <span class="w-28 text-gray-500 shrink-0">扩展名</span>
                <span>{{ previewFile.ext || '-' }}</span>
              </div>
              <div class="flex py-2 gap-4">
                <span class="w-28 text-gray-500 shrink-0">存储平台</span>
                <span>{{ previewFile.platform || '-' }}</span>
              </div>
              <div class="flex py-2 gap-4">
                <span class="w-28 text-gray-500 shrink-0">存储路径</span>
                <span class="break-all font-mono text-xs">{{ previewFile.basePath }}{{ previewFile.path }}</span>
              </div>
              <div class="flex py-2 gap-4">
                <span class="w-28 text-gray-500 shrink-0">访问 URL</span>
                <a
                  :href="previewFile.url"
                  target="_blank"
                  class="text-primary-500 hover:underline break-all text-xs"
                >{{ previewFile.url }}</a>
              </div>
              <div class="flex py-2 gap-4">
                <span class="w-28 text-gray-500 shrink-0">上传时间</span>
                <span>{{ previewFile.createTime?.replace('T', ' ').substring(0, 19) || '-' }}</span>
              </div>
            </div>
          </div>
        </template>
      </USlideover>

      <!-- 删除确认 Modal -->
      <UModal v-model:open="showDeleteModal" title="确认删除">
        <template #body>
          <p class="text-sm text-gray-600 dark:text-gray-400">
            确定要删除文件
            <strong>{{ deleteTarget?.originalFilename || deleteTarget?.filename }}</strong>
            吗？此操作将同时删除存储介质上的实体文件，不可恢复。
          </p>
        </template>
        <template #footer>
          <div class="flex justify-end gap-3">
            <UButton variant="outline" @click="showDeleteModal = false">取消</UButton>
            <UButton color="error" :loading="deleting" @click="confirmDelete">确认删除</UButton>
          </div>
        </template>
      </UModal>
    </template>
  </UDashboardPanel>
</template>


