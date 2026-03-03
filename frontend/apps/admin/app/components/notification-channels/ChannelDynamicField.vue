<script setup lang="ts">
import type { FormFieldDef } from '~/types'

/**
 * 通知渠道动态字段渲染组件（代理层）
 * 复用通用 DynamicFormField 组件，保持对外 API 不变。
 */
defineProps<{
  field: FormFieldDef
  modelValue: string | undefined
}>()

defineEmits<{ 'update:modelValue': [value: string] }>()
</script>

<template>
  <DynamicFormField
    :field="field"
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
  />
</template>
