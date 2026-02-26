<script setup lang="ts">
/**
 * OAuth2 客户端配置表单组件
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
  registrationId: z.string().min(1, '请输入注册 ID'),
  clientId: z.string().min(1, '请输入客户端 ID'),
  clientSecret: z.string().optional(),
  clientName: z.string().optional(),
  authorizationGrantType: z.string().optional(),
  clientAuthenticationMethod: z.string().optional(),
  authorizationUri: z.string().optional(),
  tokenUri: z.string().optional(),
  userInfoUri: z.string().optional(),
  userNameAttribute: z.string().optional(),
  jwkSetUri: z.string().optional(),
  scopes: z.string().optional(),
  redirectUriTemplate: z.string().optional(),
  redirectUri: z.string().optional(),
  isEnabled: z.coerce.number().default(0),
  status: z.coerce.number().default(0),
  remark: z.string().optional()
})

const enabledOptions = [
  { label: '启用', value: 0 },
  { label: '禁用', value: 1 }
]

const statusOptions = [
  { label: '启用', value: 0 },
  { label: '禁用', value: 1 }
]

const state = reactive({
  registrationId: '',
  clientId: '',
  clientSecret: '',
  clientName: '',
  authorizationGrantType: '',
  clientAuthenticationMethod: '',
  authorizationUri: '',
  tokenUri: '',
  userInfoUri: '',
  userNameAttribute: '',
  jwkSetUri: '',
  scopes: '',
  redirectUriTemplate: '',
  redirectUri: '',
  isEnabled: 0,
  status: 0,
  remark: '',
  ...props.data
})

const formRef = ref()
const showClientSecret = ref(false)

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
      <UFormField label="注册 ID" name="registrationId" required>
        <UInput
          v-model="state.registrationId"
          placeholder="如：github, google"
          class="w-full"
        />
      </UFormField>

      <UFormField label="客户端 ID" name="clientId" required>
        <UInput
          v-model="state.clientId"
          placeholder="如：abc123xyz"
          class="w-full"
        />
      </UFormField>

      <UFormField label="客户端密钥" name="clientSecret">
        <UInput
          v-model="state.clientSecret"
          :type="showClientSecret ? 'text' : 'password'"
          placeholder="请输入客户端密钥"
          :ui="{ trailing: 'pe-1' }"
          class="w-full"
        >
          <template #trailing>
            <UButton
              color="neutral"
              variant="link"
              size="sm"
              :icon="showClientSecret ? 'i-lucide-eye-off' : 'i-lucide-eye'"
              :aria-label="showClientSecret ? '隐藏密钥' : '显示密钥'"
              :aria-pressed="showClientSecret"
              @click="showClientSecret = !showClientSecret"
            />
          </template>
        </UInput>
      </UFormField>

      <UFormField label="客户端名称" name="clientName">
        <UInput
          v-model="state.clientName"
          placeholder="如：GitHub 登录"
          class="w-full"
        />
      </UFormField>

      <UFormField label="授权类型" name="authorizationGrantType">
        <UInput
          v-model="state.authorizationGrantType"
          placeholder="如：authorization_code"
          class="w-full"
        />
      </UFormField>

      <UFormField label="认证方式" name="clientAuthenticationMethod">
        <UInput
          v-model="state.clientAuthenticationMethod"
          placeholder="如：client_secret_basic"
          class="w-full"
        />
      </UFormField>

      <UFormField label="授权端点" name="authorizationUri">
        <UInput
          v-model="state.authorizationUri"
          placeholder="如：https://github.com/login/oauth/authorize"
          class="w-full"
        />
      </UFormField>

      <UFormField label="Token 端点" name="tokenUri">
        <UInput
          v-model="state.tokenUri"
          placeholder="如：https://github.com/login/oauth/access_token"
          class="w-full"
        />
      </UFormField>

      <UFormField label="用户信息端点" name="userInfoUri">
        <UInput
          v-model="state.userInfoUri"
          placeholder="如：https://api.github.com/user"
          class="w-full"
        />
      </UFormField>

      <UFormField label="用户名属性" name="userNameAttribute">
        <UInput
          v-model="state.userNameAttribute"
          placeholder="如：login, email"
          class="w-full"
        />
      </UFormField>

      <UFormField label="JWK Set 端点" name="jwkSetUri">
        <UInput
          v-model="state.jwkSetUri"
          placeholder="如：https://idp.example.com/.well-known/jwks.json"
          class="w-full"
        />
      </UFormField>
    </div>

    <UFormField label="授权范围" name="scopes">
      <UTextarea
        v-model="state.scopes"
        placeholder="多个范围以空格或逗号分隔"
        :rows="2"
        class="w-full"
      />
    </UFormField>

    <div class="grid grid-cols-2 gap-4">
      <UFormField label="重定向 URI 模板" name="redirectUriTemplate">
        <UTextarea
          v-model="state.redirectUriTemplate"
          placeholder="如：{baseUrl}/login/oauth2/code/{registrationId}"
          :rows="2"
          class="w-full"
        />
      </UFormField>

      <UFormField label="重定向 URI" name="redirectUri">
        <UTextarea
          v-model="state.redirectUri"
          placeholder="如：http://localhost:3000/login/oauth2/code/github-app"
          :rows="2"
          class="w-full"
        />
      </UFormField>

      <UFormField label="是否启用" name="isEnabled">
        <div class="flex gap-4">
          <label
            v-for="opt in enabledOptions"
            :key="opt.value"
            class="flex cursor-pointer items-center gap-1.5"
          >
            <input
              type="radio"
              :value="opt.value"
              :checked="state.isEnabled === opt.value"
              class="accent-primary"
              @change="state.isEnabled = opt.value"
            />
            <span class="text-sm">{{ opt.label }}</span>
          </label>
        </div>
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
    </div>

    <UFormField label="备注" name="remark">
      <UTextarea
        v-model="state.remark"
        placeholder="备注信息"
        :rows="2"
        class="w-full"
      />
    </UFormField>
  </UForm>
</template>
