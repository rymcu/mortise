<script setup lang="ts">
import { authorizeDesktopClient } from '@mortise/core-sdk'

definePageMeta({
  layout: 'auth',
  middleware: 'auth'
})

useSeoMeta({
  title: '授权 Rodak',
  description: '确认是否授权 Rodak 桌面端访问您的 Mortise 账号'
})

const route = useRoute()
const api = useNuxtApp().$api
const auth = useAuthStore()
const loading = ref(false)
const errorMessage = ref('')

const clientId = computed(() => stringQuery('client_id'))
const redirectUri = computed(() => stringQuery('redirect_uri'))
const state = computed(() => stringQuery('state'))
const codeChallenge = computed(() => stringQuery('code_challenge'))
const codeChallengeMethod = computed(() => stringQuery('code_challenge_method') || 'S256')
const scope = computed(() => stringQuery('scope') || 'profile')
const deviceName = computed(() => stringQuery('device_name') || 'Rodak')
const deviceFingerprint = computed(() => stringQuery('device_fingerprint'))

const canAuthorize = computed(() =>
  Boolean(clientId.value && redirectUri.value && state.value && codeChallenge.value)
)

function stringQuery(key: string): string {
  const value = route.query[key]
  return typeof value === 'string' ? value : ''
}

async function handleAuthorize() {
  errorMessage.value = ''
  if (!canAuthorize.value) {
    errorMessage.value = '授权请求缺少必要参数。'
    return
  }

  loading.value = true
  try {
    const response = await authorizeDesktopClient(api, {
      client_id: clientId.value,
      redirect_uri: redirectUri.value,
      state: state.value,
      code_challenge: codeChallenge.value,
      code_challenge_method: codeChallengeMethod.value,
      scope: scope.value,
      device_name: deviceName.value,
      device_fingerprint: deviceFingerprint.value || undefined
    })

    if (!response?.redirectUri) {
      throw new Error('授权响应缺少回调地址。')
    }

    await navigateTo(response.redirectUri, { external: true })
  }
  catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '授权 Rodak 失败。'
  }
  finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="space-y-4">
    <UCard>
      <template #header>
        <div class="flex items-center gap-3">
          <UIcon name="i-lucide-monitor-check" class="size-5 text-primary" />
          <div>
            <h1 class="text-lg font-semibold">授权 Rodak</h1>
            <p class="text-sm text-muted">
              {{ auth.currentUser?.nickname || auth.currentUser?.username || '当前账号' }}
            </p>
          </div>
        </div>
      </template>

      <div class="space-y-4">
        <p class="text-sm text-muted">
          Rodak 桌面端请求访问您的 Mortise 账号，用于同步会员身份并访问已登录接口。
        </p>

        <div class="rounded-md border border-muted p-3 text-sm">
          <div class="flex items-center justify-between gap-4">
            <span class="text-muted">客户端</span>
            <span class="font-medium">{{ clientId || '未知客户端' }}</span>
          </div>
          <div class="mt-2 flex items-center justify-between gap-4">
            <span class="text-muted">设备</span>
            <span class="font-medium">{{ deviceName }}</span>
          </div>
          <div class="mt-2 flex items-center justify-between gap-4">
            <span class="text-muted">权限</span>
            <span class="font-medium">{{ scope }}</span>
          </div>
        </div>

        <UAlert
          v-if="errorMessage"
          color="error"
          variant="soft"
          icon="i-lucide-circle-alert"
          :title="errorMessage"
        />
      </div>

      <template #footer>
        <div class="flex justify-end gap-2">
          <UButton color="neutral" variant="ghost" to="/">
            取消
          </UButton>
          <UButton
            icon="i-lucide-check"
            :loading="loading"
            :disabled="!canAuthorize"
            @click="handleAuthorize"
          >
            授权并返回 Rodak
          </UButton>
        </div>
      </template>
    </UCard>
  </div>
</template>
