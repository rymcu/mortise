<script setup lang="ts">
/**
 * 菜单管理 - 树形表格行（递归组件）
 * 用于 menus.vue 页面中渲染树形表格
 */

interface MenuTreeItem {
  id: number | string
  label?: string
  permission?: string
  href?: string
  icon?: string
  menuType?: number
  sortNo?: number
  status?: number
  children?: MenuTreeItem[]
}

type BadgeColor =
  | 'error'
  | 'info'
  | 'success'
  | 'primary'
  | 'secondary'
  | 'warning'
  | 'neutral'

const props = defineProps<{
  node: MenuTreeItem
  depth: number
  expandedIds: Set<string | number>
  formatMenuType: (type?: number) => string
  menuTypeBadgeColor: (type?: number) => BadgeColor
}>()

const emit = defineEmits<{
  (e: 'toggle-expand', id: string | number): void
  (e: 'edit' | 'delete', row: MenuTreeItem): void
}>()

const hasChildren = computed(() => (props.node.children?.length ?? 0) > 0)
const isExpanded = computed(() => props.expandedIds.has(props.node.id))
</script>

<template>
  <tr class="border-default/60 hover:bg-elevated/50 border-b transition-colors">
    <!-- 菜单名称（含缩进和展开按钮） -->
    <td class="px-2 py-2">
      <div
        class="flex items-center gap-1.5"
        :style="{ paddingLeft: `${depth * 1.25}rem` }"
      >
        <!-- 展开/折叠按钮 -->
        <button
          v-if="hasChildren"
          class="text-muted hover:text-default flex h-5 w-5 shrink-0 items-center justify-center rounded transition-colors"
          @click="emit('toggle-expand', node.id)"
        >
          <UIcon
            :name="
              isExpanded ? 'i-lucide-chevron-down' : 'i-lucide-chevron-right'
            "
            class="text-sm"
          />
        </button>
        <span v-else class="w-5 shrink-0" />

        <!-- 图标 -->
        <UIcon
          v-if="node.icon"
          :name="String(node.icon)"
          class="text-muted shrink-0 text-base"
        />

        <span class="text-sm">{{ node.label || '-' }}</span>
      </div>
    </td>

    <!-- 权限标识 -->
    <td class="text-muted px-2 py-2 text-sm">
      {{ node.permission || '-' }}
    </td>

    <!-- 路由 -->
    <td class="text-muted px-2 py-2 text-sm">
      {{ node.href || '-' }}
    </td>

    <!-- 类型 -->
    <td class="px-2 py-2 text-center">
      <UBadge
        :color="menuTypeBadgeColor(Number(node.menuType))"
        variant="subtle"
      >
        {{ formatMenuType(Number(node.menuType)) }}
      </UBadge>
    </td>

    <!-- 排序 -->
    <td class="text-muted px-2 py-2 text-center text-sm">
      {{ node.sortNo ?? '-' }}
    </td>

    <!-- 状态 -->
    <td class="px-2 py-2 text-center">
      <UBadge
        :color="node.status === 0 ? 'success' : 'neutral'"
        variant="subtle"
      >
        {{ node.status === 0 ? '启用' : '禁用' }}
      </UBadge>
    </td>

    <!-- 操作 -->
    <td class="px-2 py-2 text-right">
      <div class="flex items-center justify-end gap-1">
        <UButton
          icon="i-lucide-pencil"
          color="primary"
          variant="ghost"
          size="xs"
          @click="emit('edit', node)"
        >
          编辑
        </UButton>
        <UButton
          icon="i-lucide-trash-2"
          color="error"
          variant="ghost"
          size="xs"
          @click="emit('delete', node)"
        >
          删除
        </UButton>
      </div>
    </td>
  </tr>

  <!-- 递归渲染子节点 -->
  <template v-if="hasChildren && isExpanded">
    <MenusMenuTreeRow
      v-for="child in node.children"
      :key="String(child.id)"
      :node="child"
      :depth="depth + 1"
      :expanded-ids="expandedIds"
      :format-menu-type="formatMenuType"
      :menu-type-badge-color="menuTypeBadgeColor"
      @toggle-expand="emit('toggle-expand', $event)"
      @edit="emit('edit', $event)"
      @delete="emit('delete', $event)"
    />
  </template>
</template>
