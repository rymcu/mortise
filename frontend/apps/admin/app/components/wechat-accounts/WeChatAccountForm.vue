<script setup lang="ts">
/**
 * 微信账号表单组件
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
const showAppSecret = ref(false)

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
        <UInput
          v-model="state.accountName"
          placeholder="请输入账号名称"
          class="w-full"
        />
      </UFormField>

      <UFormField label="账号类型" name="accountType">
        <UInput
          v-model="state.accountType"
          placeholder="如：公众号、小程序"
          class="w-full"
        />
      </UFormField>

      <UFormField label="AppID" name="appId" required>
        <UInput
          v-model="state.appId"
          placeholder="请输入 AppID"
          class="w-full"
        />
      </UFormField>

      <UFormField label="AppSecret" name="appSecret">
        <UInput
          v-model="state.appSecret"
          :type="showAppSecret ? 'text' : 'password'"
          placeholder="请输入 AppSecret"
          :ui="{ trailing: 'pe-1' }"
          class="w-full"
        >
          <template #trailing>
            <UButton
              color="neutral"
              variant="link"
              size="sm"
              :icon="showAppSecret ? 'i-lucide-eye-off' : 'i-lucide-eye'"
              :aria-label="showAppSecret ? '隐藏密钥' : '显示密钥'"
              :aria-pressed="showAppSecret"
              @click="showAppSecret = !showAppSecret"
            />
          </template>
        </UInput>
      </UFormField>

      <UFormField label="是否默认" name="isDefault">
        <URadioGroup
          v-model="state.isDefault"
          :items="yesNoOptions"
          orientation="horizontal"
        />
      </UFormField>

      <UFormField label="是否启用" name="isEnabled">
        <URadioGroup
          v-model="state.isEnabled"
          :items="enabledOptions"
          orientation="horizontal"
        />
      </UFormField>

      <UFormField label="状态" name="status">
        <URadioGroup
          v-model="state.status"
          :items="statusOptions"
          orientation="horizontal"
        />
      </UFormField>
    </div>

    <UFormField label="备注" name="remark">
      <UTextarea
        v-model="state.remark"
        placeholder="备注信息"
        :rows="3"
        class="w-full"
      />
    </UFormField>
  </UForm>
</template>
