<script setup lang="ts">
/**
 * 角色表单组件
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
  label: z.string().min(1, '请输入角色名'),
  permission: z.string().min(1, '请输入权限标识')
})

const state = reactive({
  label: '',
  permission: '',
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
    <UFormField label="角色名" name="label" required>
      <UInput v-model="state.label" placeholder="请输入角色名" class="w-full" />
    </UFormField>

    <UFormField label="权限标识" name="permission" required>
      <UInput v-model="state.permission" placeholder="如：admin、editor" class="w-full" />
    </UFormField>
  </UForm>
</template>
