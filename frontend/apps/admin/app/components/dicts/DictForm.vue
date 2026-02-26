<script setup lang="ts">
/**
 * 字典表单组件
 */
import * as z from 'zod'
import { fetchAdminPage } from '@mortise/core-sdk'

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
  dictTypeCode: z.string().min(1, '请选择字典类型'),
  label: z.string().min(1, '请输入字典标签'),
  value: z.string().min(1, '请输入字典值'),
  sortNo: z.coerce.number().default(0),
  status: z.coerce.number().default(0),
  icon: z.string().optional(),
  image: z.string().optional(),
  color: z.string().optional()
})

const statusOptions = [
  { label: '启用', value: 0 },
  { label: '禁用', value: 1 }
]

const colorOptions = [
  { label: 'Primary', value: 'primary' },
  { label: 'Secondary', value: 'secondary' },
  { label: 'Success', value: 'success' },
  { label: 'Info', value: 'info' },
  { label: 'Warning', value: 'warning' },
  { label: 'Error', value: 'error' }
]

const state = reactive({
  dictTypeCode: '',
  label: '',
  value: '',
  sortNo: 0,
  status: 0,
  icon: '',
  image: '',
  color: '',
  ...props.data
})

// 加载字典类型列表
const dictTypeItems = ref<Array<{ label: string; value: string }>>([])

async function loadDictTypes() {
  try {
    const data = await fetchAdminPage<Record<string, unknown>>(
      $api,
      '/api/v1/admin/dictionary-types',
      { pageSize: 100 }
    )
    dictTypeItems.value = (data.records || []).map(
      (item: Record<string, unknown>) => ({
        label: `${item.label} (${item.typeCode})`,
        value: item.typeCode as string
      })
    )
  } catch {
    // 静默失败
  }
}

onMounted(loadDictTypes)

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
    <UFormField label="字典类型" name="dictTypeCode" required>
      <select
        v-model="state.dictTypeCode"
        class="w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm dark:border-gray-600 dark:bg-gray-800"
      >
        <option value="">请选择字典类型</option>
        <option
          v-for="item in dictTypeItems"
          :key="item.value"
          :value="item.value"
        >
          {{ item.label }}
        </option>
      </select>
    </UFormField>

    <UFormField label="字典标签" name="label" required>
      <UInput
        v-model="state.label"
        placeholder="请输入字典标签"
        class="w-full"
      />
    </UFormField>

    <UFormField label="字典值" name="value" required>
      <UInput v-model="state.value" placeholder="请输入字典值" class="w-full" />
    </UFormField>

    <UFormField label="排序号" name="sortNo">
      <UInput
        v-model.number="state.sortNo"
        type="number"
        placeholder="数字越小越靠前"
        class="w-full"
      />
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

    <UFormField label="图标" name="icon">
      <UInput
        v-model="state.icon"
        placeholder="如：i-lucide-home"
        class="w-full"
      />
    </UFormField>

    <UFormField label="图片" name="image">
      <UInput v-model="state.image" placeholder="图片 URL" class="w-full" />
    </UFormField>

    <UFormField label="颜色" name="color">
      <select
        v-model="state.color"
        class="w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm dark:border-gray-600 dark:bg-gray-800"
      >
        <option value="">无</option>
        <option v-for="opt in colorOptions" :key="opt.value" :value="opt.value">
          {{ opt.label }}
        </option>
      </select>
    </UFormField>
  </UForm>
</template>
