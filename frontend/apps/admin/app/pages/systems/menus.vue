<script setup lang="ts">
/**
 * 菜单管理 - 树形展示
 * 通过 /api/v1/admin/menus/tree 接口获取树形菜单数据
 */
import { fetchAdminGet } from '@mortise/core-sdk'

interface MenuTreeItem {
  id: number | string
  label?: string
  permission?: string
  href?: string
  icon?: string
  menuType?: number
  sortNo?: number
  status?: number
  parentId?: number | string
  children?: MenuTreeItem[]
}

const { $api } = useNuxtApp()

const loading = ref(false)
const errorMessage = ref('')
const menuTree = ref<MenuTreeItem[]>([])
const expandedIds = ref<Set<string | number>>(new Set())
const keyword = ref('')

async function loadMenus() {
  loading.value = true
  errorMessage.value = ''
  try {
    const tree = await fetchAdminGet<MenuTreeItem[]>(
      $api,
      '/api/v1/admin/menus/tree'
    )
    menuTree.value = tree || []
    // 默认展开所有含子节点的节点
    const ids = new Set<string | number>()
    const walk = (nodes: MenuTreeItem[]) => {
      for (const n of nodes) {
        if (n.children?.length) {
          ids.add(n.id)
          walk(n.children)
        }
      }
    }
    walk(menuTree.value)
    expandedIds.value = ids
  } catch (err) {
    errorMessage.value = err instanceof Error ? err.message : '加载菜单失败'
  } finally {
    loading.value = false
  }
}

await loadMenus()

/** 展平树获取总数 */
function flatCount(nodes: MenuTreeItem[]): number {
  let count = 0
  for (const n of nodes) {
    count++
    if (n.children?.length) count += flatCount(n.children)
  }
  return count
}

const totalCount = computed(() => flatCount(menuTree.value))

/** 按关键词过滤菜单树（保留匹配节点及其祖先） */
const filteredTree = computed(() => {
  if (!keyword.value.trim()) return menuTree.value
  const kw = keyword.value.trim().toLowerCase()

  function filterNodes(nodes: MenuTreeItem[]): MenuTreeItem[] {
    const result: MenuTreeItem[] = []
    for (const node of nodes) {
      const childMatches = node.children?.length
        ? filterNodes(node.children)
        : []
      const selfMatch =
        (node.label || '').toLowerCase().includes(kw) ||
        (node.permission || '').toLowerCase().includes(kw)
      if (selfMatch || childMatches.length > 0) {
        result.push({
          ...node,
          children: childMatches.length > 0 ? childMatches : node.children
        })
      }
    }
    return result
  }
  return filterNodes(menuTree.value)
})

function toggleExpand(id: string | number) {
  const s = new Set(expandedIds.value)
  if (s.has(id)) {
    s.delete(id)
  } else {
    s.add(id)
  }
  expandedIds.value = s
}

function expandAll() {
  const ids = new Set<string | number>()
  const walk = (nodes: MenuTreeItem[]) => {
    for (const n of nodes) {
      if (n.children?.length) {
        ids.add(n.id)
        walk(n.children)
      }
    }
  }
  walk(menuTree.value)
  expandedIds.value = ids
}

function collapseAll() {
  expandedIds.value = new Set()
}

function formatMenuType(type?: number): string {
  if (type === 0) return '目录'
  if (type === 1) return '菜单'
  if (type === 2) return '按钮'
  return '-'
}

type BadgeColor =
  | 'error'
  | 'info'
  | 'success'
  | 'primary'
  | 'secondary'
  | 'warning'
  | 'neutral'

function menuTypeBadgeColor(type?: number): BadgeColor {
  if (type === 0) return 'info'
  if (type === 1) return 'success'
  if (type === 2) return 'warning'
  return 'neutral'
}

// CRUD 弹窗状态
const showAddModal = ref(false)
const showEditModal = ref(false)
const showDeleteModal = ref(false)
const currentRow = ref<Record<string, unknown>>({})

function openEditModal(row: MenuTreeItem) {
  currentRow.value = { ...row } as unknown as Record<string, unknown>
  showEditModal.value = true
}

function openDeleteModal(row: MenuTreeItem) {
  currentRow.value = { ...row } as unknown as Record<string, unknown>
  showDeleteModal.value = true
}
</script>

<template>
  <UDashboardPanel id="system-menus">
    <template #header>
      <UDashboardNavbar title="菜单管理">
        <template #leading>
          <UDashboardSidebarCollapse />
        </template>
      </UDashboardNavbar>
    </template>

    <template #body>
      <div>
        <UAlert
          v-if="errorMessage"
          color="error"
          variant="soft"
          :title="errorMessage"
          class="mb-4"
        />

        <UCard>
          <!-- 工具栏 -->
          <div class="mb-4 flex flex-wrap items-center justify-between gap-2">
            <div class="flex flex-wrap items-center gap-2">
              <UInput
                v-model="keyword"
                placeholder="搜索菜单名/权限"
                icon="i-lucide-search"
                class="w-72"
              />
              <UButton
                color="neutral"
                variant="soft"
                icon="i-lucide-refresh-cw"
                :loading="loading"
                @click="loadMenus"
              >
                刷新
              </UButton>
              <UButton
                color="neutral"
                variant="ghost"
                size="sm"
                @click="expandAll"
              >
                全部展开
              </UButton>
              <UButton
                color="neutral"
                variant="ghost"
                size="sm"
                @click="collapseAll"
              >
                全部折叠
              </UButton>
            </div>
            <div class="flex items-center gap-2">
              <UButton
                icon="i-lucide-plus"
                color="primary"
                variant="soft"
                @click="showAddModal = true"
              >
                新增菜单
              </UButton>
            </div>
          </div>

          <!-- 树形表格 -->
          <div class="overflow-x-auto">
            <table class="min-w-full text-sm">
              <thead>
                <tr class="border-default border-b">
                  <th class="px-2 py-2 text-left" style="min-width: 280px">
                    菜单名称
                  </th>
                  <th class="px-2 py-2 text-left">权限标识</th>
                  <th class="px-2 py-2 text-left">路由</th>
                  <th class="px-2 py-2 text-center">类型</th>
                  <th class="px-2 py-2 text-center">排序</th>
                  <th class="px-2 py-2 text-center">状态</th>
                  <th class="px-2 py-2 text-right">操作</th>
                </tr>
              </thead>
              <tbody>
                <template
                  v-for="rootNode in filteredTree"
                  :key="String(rootNode.id)"
                >
                  <MenusMenuTreeRow
                    :node="rootNode"
                    :depth="0"
                    :expanded-ids="expandedIds"
                    :format-menu-type="formatMenuType"
                    :menu-type-badge-color="menuTypeBadgeColor"
                    @toggle-expand="toggleExpand"
                    @edit="openEditModal"
                    @delete="openDeleteModal"
                  />
                </template>
                <tr v-if="!filteredTree.length && !loading">
                  <td colspan="7" class="text-muted py-6 text-center">
                    暂无菜单数据
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="text-muted mt-4 flex items-center text-sm">
            <span>共 {{ totalCount }} 个菜单</span>
          </div>
        </UCard>

        <!-- 弹窗 -->
        <MenusMenuAddModal v-model:open="showAddModal" @success="loadMenus" />
        <MenusMenuEditModal
          v-model:open="showEditModal"
          :menu="currentRow"
          @success="loadMenus"
        />
        <MenusMenuDeleteModal
          v-model:open="showDeleteModal"
          :menu="currentRow"
          @success="loadMenus"
        />
      </div>
    </template>
  </UDashboardPanel>
</template>
