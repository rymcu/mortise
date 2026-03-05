<script setup lang="ts">
/**
 * 产品分类表单组件
 */
import * as z from 'zod'

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

const schema = z.object({
  name: z.string().min(1, '请输入分类名称'),
  slug: z.string().min(1, '请输入分类别名'),
  description: z.string().optional(),
  sortNo: z.coerce.number().default(0),
  status: z.coerce.number().default(0)
})

const statusOptions = [
  { label: '正常', value: 0 },
  { label: '禁用', value: 1 }
]

const state = reactive({
  name: '',
  slug: '',
  description: '',
  parentId: null as number | null,
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
    <UFormField label="分类名称" name="name" required>
      <UInput v-model="state.name" placeholder="如：电子产品" class="w-full" />
    </UFormField>

    <UFormField label="分类别名" name="slug" required>
      <UInput
        v-model="state.slug"
        placeholder="如：electronics（URL 友好格式）"
        class="w-full"
      />
    </UFormField>

    <UFormField label="描述" name="description">
      <UTextarea
        v-model="state.description"
        placeholder="请输入分类描述"
        :rows="3"
        class="w-full"
      />
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
          class="flex cursor-pointer items-center gap-2"
        >
          <input
            v-model.number="state.status"
            type="radio"
            :value="opt.value"
            class="accent-primary"
          />
          <span class="text-sm">{{ opt.label }}</span>
        </label>
      </div>
    </UFormField>
  </UForm>
</template>
