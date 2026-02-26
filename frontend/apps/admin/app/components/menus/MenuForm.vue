<script setup lang="ts">
/**
 * 菜单表单组件
 */
import * as z from 'zod'
import { fetchAdminGet } from '@mortise/core-sdk'

const props = withDefaults(
  defineProps<{
    data?: Record<string, unknown>
  }>(),
  {
    data: () => ({})
  }
)

const emit = defineEmits<{
  (e: 'change', data: Record<string, unknown>): void
}>()

const { $api } = useNuxtApp()

const schema = z.object({
  label: z.string().min(1, '请输入菜单名称'),
  permission: z.string().optional(),
  icon: z.string().optional(),
  href: z.string().optional(),
  menuType: z.coerce.number().default(0),
  status: z.coerce.number().default(0),
  parentId: z.union([z.string(), z.number()]).optional(),
  sortNo: z.coerce.number().default(0)
})

const menuTypeOptions = [
  { label: '目录', value: 0 },
  { label: '菜单', value: 1 },
  { label: '按钮', value: 2 }
]

const statusOptions = [
  { label: '启用', value: 0 },
  { label: '禁用', value: 1 }
]

const state = reactive({
  label: '',
  permission: '',
  icon: '',
  href: '',
  menuType: 0,
  status: 0,
  parentId: undefined as string | number | undefined,
  sortNo: 0,
  ...props.data
})

// 加载菜单树作为父级菜单选项
const parentMenuItems = ref<Array<{ label: string; value: string | number }>>(
  []
)

async function loadMenuTree() {
  try {
    const data = await fetchAdminGet<Array<Record<string, unknown>>>(
      $api,
      '/api/v1/admin/menus/tree'
    )
    const items: Array<{ label: string; value: string | number }> = [
      { label: '根菜单', value: '0' }
    ]
    function flatten(nodes: Array<Record<string, unknown>>, depth = 0) {
      for (const node of nodes) {
        const prefix = depth > 0 ? '　'.repeat(depth) + '└ ' : ''
        items.push({
          label: `${prefix}${node.label}`,
          value: node.id as string | number
        })
        if (Array.isArray(node.children) && node.children.length > 0) {
          flatten(node.children as Array<Record<string, unknown>>, depth + 1)
        }
      }
    }
    flatten(data)
    parentMenuItems.value = items
  } catch {
    // 静默失败
  }
}

onMounted(loadMenuTree)

const formRef = ref()

watch(state, (v) => emit('change', { ...v }), { deep: true })

async function validate(): Promise<boolean> {
  try {
    if (formRef.value?.validate) {
      await formRef.value.validate()
    }
    return schema.safeParse(state).success
  } catch {
    return false
  }
}

defineExpose({ validate, state })
</script>

<template>
  <UForm ref="formRef" :schema="schema" :state="state" class="space-y-4">
    <UFormField label="菜单名称" name="label" required>
      <UInput
        v-model="state.label"
        placeholder="请输入菜单名称"
        class="w-full"
      />
    </UFormField>

    <UFormField label="权限标识" name="permission">
      <UInput
        v-model="state.permission"
        placeholder="如：system:menu:list"
        class="w-full"
      />
    </UFormField>

    <UFormField label="图标" name="icon">
      <UInput
        v-model="state.icon"
        placeholder="如：i-lucide-home"
        class="w-full"
      />
    </UFormField>

    <UFormField label="路由链接" name="href">
      <UInput
        v-model="state.href"
        placeholder="如：/systems/menus"
        class="w-full"
      />
    </UFormField>

    <UFormField label="菜单类型" name="menuType">
      <div class="flex gap-4">
        <label
          v-for="opt in menuTypeOptions"
          :key="opt.value"
          class="flex cursor-pointer items-center gap-1.5"
        >
          <input
            type="radio"
            :value="opt.value"
            :checked="state.menuType === opt.value"
            class="accent-primary"
            @change="state.menuType = opt.value"
          />
          <span class="text-sm">{{ opt.label }}</span>
        </label>
      </div>
    </UFormField>

    <UFormField label="状态" name="status">
      <div class="flex gap-4">
        <label
          v-for="opt in statusOptions"
          :key="opt.value"
          class="flex cursor-pointer items-center gap-1.5"
        >
          <input
            type="radio"
            :value="opt.value"
            :checked="state.status === opt.value"
            class="accent-primary"
            @change="state.status = opt.value"
          />
          <span class="text-sm">{{ opt.label }}</span>
        </label>
      </div>
    </UFormField>

    <UFormField label="上级菜单" name="parentId">
      <select
        v-model="state.parentId"
        class="w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm dark:border-gray-600 dark:bg-gray-800"
      >
        <option :value="undefined">无</option>
        <option
          v-for="item in parentMenuItems"
          :key="item.value"
          :value="item.value"
        >
          {{ item.label }}
        </option>
      </select>
    </UFormField>

    <UFormField label="排序号" name="sortNo">
      <UInput
        v-model.number="state.sortNo"
        type="number"
        placeholder="数字越小越靠前"
        class="w-full"
      />
    </UFormField>
  </UForm>
</template>
