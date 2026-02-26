<script setup lang="ts">
/**
 * 用户表单组件
 * 用于新增/编辑用户的表单字段
 */
import * as z from 'zod'

const props = withDefaults(
  defineProps<{
    data?: Record<string, unknown>
    /** 是否为编辑模式（隐藏密码字段为可选） */
    editMode?: boolean
  }>(),
  {
    data: () => ({}),
    editMode: false
  }
)

const emit = defineEmits<{
  (e: 'change', data: Record<string, unknown>): void
}>()

const schema = z.object({
  nickname: z.string().min(1, '请输入昵称').max(30, '昵称最多 30 个字符'),
  email: z.string().email('请输入有效的邮箱').optional().or(z.literal('')),
  phone: z.string().max(20, '手机号过长').optional().or(z.literal('')),
  password: props.editMode
    ? z.string().optional().or(z.literal(''))
    : z.string().min(6, '密码至少 6 个字符')
})

const state = reactive({
  nickname: '',
  email: '',
  phone: '',
  password: '',
  ...props.data
})

const formRef = ref()
const showPassword = ref(false)

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
    <UFormField label="昵称" name="nickname" required>
      <UInput
        v-model="state.nickname"
        placeholder="请输入昵称"
        class="w-full"
      />
    </UFormField>

    <UFormField label="邮箱" name="email">
      <UInput
        v-model="state.email"
        type="email"
        placeholder="请输入邮箱"
        class="w-full"
      />
    </UFormField>

    <UFormField label="手机号" name="phone">
      <UInput v-model="state.phone" placeholder="请输入手机号" class="w-full" />
    </UFormField>

    <UFormField v-if="!editMode" label="密码" name="password" required>
      <UInput
        v-model="state.password"
        :type="showPassword ? 'text' : 'password'"
        placeholder="请输入密码"
        :ui="{ trailing: 'pe-1' }"
        class="w-full"
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
    </UFormField>
  </UForm>
</template>
