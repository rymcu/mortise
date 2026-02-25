<script setup lang="ts">
/**
 * 微信账号表单组件
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
  accountName: z.string().min(1, '请输入账号名称'),
  accountType: z.string().optional(),
  appId: z.string().min(1, '请输入 AppID'),
  appSecret: z.string().optional(),
  isDefault: z.coerce.number().default(0),
  isEnabled: z.coerce.number().default(0),
  status: z.coerce.number().default(0),
  remark: z.string().optional()
})

const yesNoOptions = [
  { label: '是', value: 0 },
  { label: '否', value: 1 }
]

const enabledOptions = [
  { label: '启用', value: 0 },
  { label: '禁用', value: 1 }
]

const statusOptions = [
  { label: '启用', value: 0 },
  { label: '禁用', value: 1 }
]

const state = reactive({
  accountName: '',
  accountType: '',
  appId: '',
  appSecret: '',
  isDefault: 1,
  isEnabled: 0,
  status: 0,
  remark: '',
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
    <div class="grid grid-cols-2 gap-4">
      <UFormField label="账号名称" name="accountName" required>
        <UInput v-model="state.accountName" placeholder="请输入账号名称" class="w-full" />
      </UFormField>

      <UFormField label="账号类型" name="accountType">
        <UInput v-model="state.accountType" placeholder="如：公众号、小程序" class="w-full" />
      </UFormField>

      <UFormField label="AppID" name="appId" required>
        <UInput v-model="state.appId" placeholder="请输入 AppID" class="w-full" />
      </UFormField>

      <UFormField label="AppSecret" name="appSecret">
        <UInput v-model="state.appSecret" type="password" placeholder="请输入 AppSecret" class="w-full" />
      </UFormField>

      <UFormField label="是否默认" name="isDefault">
        <div class="flex gap-4">
          <label v-for="opt in yesNoOptions" :key="opt.value" class="flex items-center gap-1.5 cursor-pointer">
            <input
              type="radio"
              :value="opt.value"
              :checked="state.isDefault === opt.value"
              @change="state.isDefault = opt.value"
              class="accent-primary"
            />
            <span class="text-sm">{{ opt.label }}</span>
          </label>
        </div>
      </UFormField>

      <UFormField label="是否启用" name="isEnabled">
        <div class="flex gap-4">
          <label v-for="opt in enabledOptions" :key="opt.value" class="flex items-center gap-1.5 cursor-pointer">
            <input
              type="radio"
              :value="opt.value"
              :checked="state.isEnabled === opt.value"
              @change="state.isEnabled = opt.value"
              class="accent-primary"
            />
            <span class="text-sm">{{ opt.label }}</span>
          </label>
        </div>
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
    </div>

    <UFormField label="备注" name="remark">
      <UTextarea v-model="state.remark" placeholder="备注信息" :rows="3" class="w-full" />
    </UFormField>
  </UForm>
</template>
