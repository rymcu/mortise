<script setup lang="ts">
/**
 * 角色菜单绑定弹窗
 * 使用菜单树结构展示，支持父子级联选择：
 * - 选中父节点 → 自动选中所有子节点
 * - 取消叶子节点 → 自动取消所有祖先节点
 */
import { fetchAdminGet, fetchAdminPut } from '@mortise/core-sdk'

interface MenuTreeItem {
  id: string | number
  label?: string
  permission?: string
  icon?: string
  menuType?: number
  sortNo?: number
  children?: MenuTreeItem[]
}

const props = defineProps<{
  open: boolean
  role: Record<string, unknown>
}>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'success'): void
}>()

const { $api } = useNuxtApp()

const isOpen = computed({
  get: () => props.open,
  set: (v) => emit('update:open', v)
})

const loading = ref(false)
const dataLoading = ref(false)
const errorMessage = ref('')
const menuTree = ref<MenuTreeItem[]>([])
const checkedIds = ref<Set<string | number>>(new Set())
const expandedIds = ref<Set<string | number>>(new Set())

// ============ 工具方法 ============

/** 展平菜单树为一维数组 */
function flattenTree(nodes: MenuTreeItem[]): MenuTreeItem[] {
  const result: MenuTreeItem[] = []
  const walk = (items: MenuTreeItem[]) => {
    for (const item of items) {
      result.push(item)
      if (item.children?.length) walk(item.children)
    }
  }
  walk(nodes)
  return result
}

/** 收集节点及其所有后代 */
function collectSubtree(node: MenuTreeItem): MenuTreeItem[] {
  const result: MenuTreeItem[] = []
  const walk = (n: MenuTreeItem) => {
    result.push(n)
    if (n.children?.length) n.children.forEach(walk)
  }
  walk(node)
  return result
}

/** 查找目标节点的所有祖先节点 */
function findAncestors(
  tree: MenuTreeItem[],
  targetId: string | number
): MenuTreeItem[] {
  const path: MenuTreeItem[] = []
  const find = (nodes: MenuTreeItem[], stack: MenuTreeItem[]): boolean => {
    for (const n of nodes) {
      const nextStack = [...stack, n]
      if (n.id === targetId) {
        path.push(...stack)
        return true
      }
      if (n.children?.length && find(n.children, nextStack)) return true
    }
    return false
  }
  find(tree, [])
  return path
}

// ============ 数据加载 ============

async function loadData() {
  if (!props.role?.id) return
  dataLoading.value = true
  errorMessage.value = ''
  try {
    const [tree, roleMenus] = await Promise.all([
      fetchAdminGet<MenuTreeItem[]>($api, '/api/v1/admin/menus/tree'),
      fetchAdminGet<MenuTreeItem[]>(
        $api,
        `/api/v1/admin/roles/${props.role.id}/menus`
      )
    ])
    menuTree.value = tree || []
    // 初始化选中状态：仅保留菜单树中存在的 ID
    const allIds = new Set(flattenTree(menuTree.value).map((m) => m.id))
    checkedIds.value = new Set(
      (roleMenus || []).filter((m) => allIds.has(m.id)).map((m) => m.id)
    )
    // 默认展开所有节点
    expandedIds.value = new Set(
      flattenTree(menuTree.value)
        .filter((m) => m.children?.length)
        .map((m) => m.id)
    )
  } catch (err) {
    errorMessage.value = err instanceof Error ? err.message : '加载菜单失败'
  } finally {
    dataLoading.value = false
  }
}

// ============ 选择逻辑 ============

function toggleMenu(node: MenuTreeItem) {
  const s = new Set(checkedIds.value)
  const wasChecked = s.has(node.id)

  if (wasChecked) {
    // 取消选中：取消自身 + 所有子节点 + 取消祖先
    const subtree = collectSubtree(node)
    subtree.forEach((n) => s.delete(n.id))
    // 取消所有祖先节点
    const ancestors = findAncestors(menuTree.value, node.id)
    ancestors.forEach((a) => s.delete(a.id))
  } else {
    // 选中：选中自身 + 所有子节点
    const subtree = collectSubtree(node)
    subtree.forEach((n) => s.add(n.id))
    // 检查祖先：如果所有兄弟都已选中，则自动选中父节点
    autoCheckAncestors(s, menuTree.value, node.id)
  }

  checkedIds.value = s
}

/** 向上检查：如果某个节点的所有子节点都已选中，则自动选中该节点 */
function autoCheckAncestors(
  ids: Set<string | number>,
  tree: MenuTreeItem[],
  targetId: string | number
) {
  const ancestors = findAncestors(tree, targetId)
  for (let i = ancestors.length - 1; i >= 0; i--) {
    const parent: MenuTreeItem | undefined = ancestors[i]
    if (!parent) continue
    if (parent.children?.length) {
      const allChildrenChecked = parent.children.every((c) => ids.has(c.id))
      if (allChildrenChecked) {
        ids.add(parent.id)
      }
    }
  }
}

function toggleExpand(node: MenuTreeItem) {
  const s = new Set(expandedIds.value)
  if (s.has(node.id)) {
    s.delete(node.id)
  } else {
    s.add(node.id)
  }
  expandedIds.value = s
}

// ============ 提交 ============

async function onSubmit() {
  if (!props.role?.id) return
  loading.value = true
  errorMessage.value = ''
  try {
    await fetchAdminPut($api, `/api/v1/admin/roles/${props.role.id}/menus`, {
      idRole: String(props.role.id),
      idMenus: Array.from(checkedIds.value).map(String)
    })
    isOpen.value = false
    emit('success')
  } catch (err) {
    errorMessage.value = err instanceof Error ? err.message : '保存失败'
  } finally {
    loading.value = false
  }
}

watch(
  () => props.open,
  (val) => {
    if (val) loadData()
  }
)

/** 判断节点是否"半选"（部分子节点已选中但非全部） */
function isIndeterminate(node: MenuTreeItem): boolean {
  if (!node.children?.length) return false
  const allDescendants = flattenTree(node.children)
  const checkedCount = allDescendants.filter((d) =>
    checkedIds.value.has(d.id)
  ).length
  return checkedCount > 0 && checkedCount < allDescendants.length
}
</script>

<template>
  <UModal
    v-model:open="isOpen"
    title="配置菜单"
    :ui="{ content: 'sm:max-w-2xl' }"
  >
    <template #body>
      <div class="space-y-4">
        <p class="text-muted text-sm">为角色「{{ role.label }}」分配菜单权限</p>

        <UAlert
          v-if="errorMessage"
          color="error"
          variant="soft"
          :title="errorMessage"
        />

        <div v-if="dataLoading" class="flex h-32 items-center justify-center">
          <span class="text-muted text-sm">加载菜单树中...</span>
        </div>

        <div v-else class="max-h-[50vh] overflow-y-auto">
          <div
            v-if="!menuTree.length"
            class="text-muted py-4 text-center text-sm"
          >
            暂无菜单数据
          </div>
          <!-- 递归渲染菜单树 -->
          <template v-for="node in menuTree" :key="String(node.id)">
            <RolesMenuTreeNode
              :node="node"
              :depth="0"
              :checked-ids="checkedIds"
              :expanded-ids="expandedIds"
              :is-indeterminate="isIndeterminate"
              @toggle-check="toggleMenu"
              @toggle-expand="toggleExpand"
            />
          </template>
        </div>

        <div class="text-muted text-xs">
          已选择 {{ checkedIds.size }} 个菜单
        </div>
      </div>
    </template>
    <template #footer>
      <div class="flex w-full justify-end gap-2">
        <UButton color="primary" :loading="loading" @click="onSubmit">
          保存
        </UButton>
        <UButton
          color="neutral"
          variant="subtle"
          :disabled="loading"
          @click="isOpen = false"
        >
          取消
        </UButton>
      </div>
    </template>
  </UModal>
</template>
