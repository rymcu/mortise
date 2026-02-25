<script setup lang="ts">
/**
 * 字典类型表单组件
 */
import * as z from 'zod'

const props = withDefaults(defineProps<{
  data?: Record<string, unknown>
}>(), {
  data: () => ({})
})

const emit = defineEmits<{
  (e: 'change', data: Record<string, unknown>): void
}>()

const schema = z.object({
  typeCode: z.string().min(1, '请输入类型编码'),
  label: z.string().min(1, '请输入类型名称'),
  description: z.string().optional(),
  sortNo: z.coerce.number().default(0),
  status: z.coerce.number().default(0)
})

const statusOptions = [
  { label: '启用', value: 0 },
  { label: '禁用', value: 1 }
]

const state = reactive({
  typeCode: '',
  label: '',
  description: '',
  sortNo: 0,
  status: 0,
  ...props.data
})

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
    <UFormField label="类型编码" name="typeCode" required>
      <UInput v-model="state.typeCode" placeholder="如：Status" class="w-full" />
    </UFormField>

    <UFormField label="类型名称" name="label" required>
      <UInput v-model="state.label" placeholder="请输入类型名称" class="w-full" />
    </UFormField>

    <UFormField label="描述" name="description">
      <UTextarea v-model="state.description" placeholder="请输入描述" :rows="3" class="w-full" />
    </UFormField>

    <UFormField label="排序号" name="sortNo">
      <UInput v-model.number="state.sortNo" type="number" placeholder="数字越小越靠前" class="w-full" />
    </UFormField>

    <UFormField label="状态" name="status">
      <div class="flex gap-4">
        <label v-for="opt in statusOptions" :key="opt.value" class="flex items-center gap-1.5 cursor-pointer">
          <input
            type="radio"
            :value="opt.value"
            :checked="state.status === opt.value"
            @change="state.status = opt.value"
            class="accent-primary"
          />
          <span class="text-sm">{{ opt.label }}</span>
        </label>
      </div>
    </UFormField>
  </UForm>
</template>
