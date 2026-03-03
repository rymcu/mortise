<script setup lang="ts">
import type { ChannelFieldDef } from '~/types'

/**
 * 动态字段渲染组件
 * 根据字段类型（FormFieldType）渲染对应的表单控件
 */
defineProps<{
  field: ChannelFieldDef
  modelValue: string | undefined
}>()

defineEmits<{ 'update:modelValue': [value: string] }>()

const showPassword = ref(false)
</script>

<template>
  <UFormField :label="field.label" :required="field.required">
    <!-- 布尔开关 -->
    <USwitch
      v-if="field.type === 'BOOLEAN'"
      :model-value="modelValue === 'true'"
      @update:model-value="$emit('update:modelValue', $event ? 'true' : 'false')"
    />
    <!-- 下拉选择 -->
    <USelect
      v-else-if="field.type === 'SELECT'"
      :model-value="modelValue"
      :items="field.options ?? []"
      value-key="value"
      class="w-full"
      @update:model-value="$emit('update:modelValue', $event as string)"
    />
    <!-- 密码 -->
    <UInput
      v-else-if="field.type === 'PASSWORD'"
      :model-value="modelValue"
      :type="showPassword ? 'text' : 'password'"
      :placeholder="field.placeholder"
      autocomplete="new-password"
      :ui="{ trailing: 'pe-1' }"
      class="w-full"
      @update:model-value="$emit('update:modelValue', $event)"
    >
      <template #trailing>
        <UButton
          color="neutral"
          variant="link"
          size="sm"
          :icon="showPassword ? 'i-lucide-eye-off' : 'i-lucide-eye'"
          :aria-label="showPassword ? '隐藏密码' : '显示密码'"
          :aria-pressed="showPassword"
          @click="showPassword = !showPassword"
        />
      </template>
    </UInput>
    <!-- TEXT / EMAIL / NUMBER -->
    <UInput
      v-else
      :model-value="modelValue"
      :type="field.type.toLowerCase()"
      :placeholder="field.placeholder"
      class="w-full"
      @update:model-value="$emit('update:modelValue', $event)"
    />
  </UFormField>
</template>
