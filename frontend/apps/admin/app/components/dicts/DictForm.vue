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

const emptyColorValue = '__none__'
const colorItems = [
  { label: '无', value: emptyColorValue },
  ...colorOptions
]

const selectedColor = computed({
  get: () => state.color || emptyColorValue,
  set: (value: string) => {
    state.color = value === emptyColorValue ? '' : value
  }
})

// 加载字典类型列表
const dictTypeItems = ref<Array<{ label: string; value: string }>>([])

async function loadDictTypes() {
  try {
    const data = await fetchAdminPage<Record<string, unknown>>(
      $api,
      '/api/v1/admin/dictionary-types',
      { pageNumber: 1, pageSize: 100 }
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
      <USelect
        v-model="state.dictTypeCode"
        :items="dictTypeItems"
        value-key="value"
        label-key="label"
        placeholder="请选择字典类型"
        class="w-full"
      />
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
      <URadioGroup
        v-model="state.status"
        :items="statusOptions"
        orientation="horizontal"
      />
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
      <USelect
        v-model="selectedColor"
        :items="colorItems"
        value-key="value"
        label-key="label"
        class="w-full"
      />
    </UFormField>
  </UForm>
</template>
