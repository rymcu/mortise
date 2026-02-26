<script setup lang="ts">
/**
 * 菜单树节点（递归组件）
 * 用于 RoleMenuModal 中渲染树形菜单选择
 */

interface MenuTreeItem {
  id: string | number
  label?: string
  permission?: string
  icon?: string
  menuType?: number
  children?: MenuTreeItem[]
}

const props = defineProps<{
  node: MenuTreeItem
  depth: number
  checkedIds: Set<string | number>
  expandedIds: Set<string | number>
  isIndeterminate: (node: MenuTreeItem) => boolean
}>()

const emit = defineEmits<{
  (e: 'toggle-check' | 'toggle-expand', node: MenuTreeItem): void
}>()

const hasChildren = computed(() => (props.node.children?.length ?? 0) > 0)
const isExpanded = computed(() => props.expandedIds.has(props.node.id))
const isChecked = computed(() => props.checkedIds.has(props.node.id))

function formatMenuType(type?: number): string {
  if (type === 0) return '目录'
  if (type === 1) return '菜单'
  if (type === 2) return '按钮'
  return ''
}

type BadgeColor =
  | 'info'
  | 'success'
  | 'warning'
  | 'neutral'
  | 'primary'
  | 'secondary'
  | 'error'

function menuTypeColor(type?: number): BadgeColor {
  if (type === 0) return 'info'
  if (type === 1) return 'success'
  if (type === 2) return 'warning'
  return 'neutral'
}
</script>

<template>
  <div>
    <!-- 当前节点行 -->
    <div
      class="hover:bg-elevated/50 flex cursor-pointer items-center gap-2 rounded px-2 py-1.5 transition-colors"
      :style="{ paddingLeft: `${depth * 1.25 + 0.5}rem` }"
      @click="emit('toggle-check', node)"
    >
      <!-- 展开/折叠按钮 -->
      <button
        v-if="hasChildren"
        class="text-muted hover:text-default flex h-5 w-5 items-center justify-center rounded transition-colors"
        @click.stop="emit('toggle-expand', node)"
      >
        <UIcon
          :name="
            isExpanded ? 'i-lucide-chevron-down' : 'i-lucide-chevron-right'
          "
          class="text-sm"
        />
      </button>
      <span v-else class="w-5" />

      <!-- 复选框 -->
      <input
        type="checkbox"
        :checked="isChecked"
        :indeterminate="isIndeterminate(node)"
        class="border-default rounded"
        @click.stop="emit('toggle-check', node)"
      />

      <!-- 图标 -->
      <UIcon
        v-if="node.icon"
        :name="String(node.icon)"
        class="text-muted text-base"
      />

      <!-- 标签 -->
      <span class="text-sm">{{ node.label }}</span>

      <!-- 权限标识 -->
      <span v-if="node.permission" class="text-muted ml-1 text-xs">
        {{ node.permission }}
      </span>

      <!-- 类型徽章 -->
      <UBadge
        v-if="node.menuType !== undefined"
        :color="menuTypeColor(Number(node.menuType)) as any"
        variant="subtle"
        size="xs"
      >
        {{ formatMenuType(Number(node.menuType)) }}
      </UBadge>
    </div>

    <!-- 子节点 -->
    <template v-if="hasChildren && isExpanded">
      <RolesMenuTreeNode
        v-for="child in node.children"
        :key="String(child.id)"
        :node="child"
        :depth="depth + 1"
        :checked-ids="checkedIds"
        :expanded-ids="expandedIds"
        :is-indeterminate="isIndeterminate"
        @toggle-check="emit('toggle-check', $event)"
        @toggle-expand="emit('toggle-expand', $event)"
      />
    </template>
  </div>
</template>
