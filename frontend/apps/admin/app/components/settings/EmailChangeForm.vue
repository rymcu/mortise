<script setup lang="ts">
/**
 * 更换邮箱表单组件
 * 内部使用 useProfile() 调用邮箱更换相关 API
 */
defineProps<{
  currentEmail: string
  loading: boolean
}>()

const emit = defineEmits<{
  success: []
}>()

const { sendEmailUpdateCode, confirmEmailUpdate } = useProfile()

const emailState = reactive({
  newEmail: '',
  code: ''
})

const codeSent = ref(false)
const emailSubmitError = ref('')
const emailSubmitSuccess = ref('')
const countdown = ref(0)

let countdownTimer: ReturnType<typeof setInterval> | null = null

const validNewEmail = computed(() =>
  /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(emailState.newEmail.trim())
)

function startCountdown() {
  countdown.value = 60
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(countdownTimer!)
      countdownTimer = null
    }
  }, 1000)
}

async function onSendCode() {
  emailSubmitError.value = ''
  const ok = await sendEmailUpdateCode(emailState.newEmail.trim())
  if (ok) {
    codeSent.value = true
    startCountdown()
  } else {
    emailSubmitError.value = '验证码发送失败，请检查邮箱地址后重试'
  }
}

async function onEmailSubmit() {
  emailSubmitError.value = ''
  emailSubmitSuccess.value = ''

  if (!emailState.code.trim()) {
    emailSubmitError.value = '请输入验证码'
    return
  }

  const ok = await confirmEmailUpdate(emailState.newEmail.trim(), emailState.code.trim())
  if (ok) {
    emailSubmitSuccess.value = '邮箱更换成功！'
    codeSent.value = false
    emailState.newEmail = ''
    emailState.code = ''
    if (countdownTimer) {
      clearInterval(countdownTimer)
      countdown.value = 0
    }
    emit('success')
  } else {
    emailSubmitError.value = '验证码错误或已失效，请重新发送'
  }
}

onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer)
})
</script>

<template>
  <UCard>
    <template #header>
      <h3 class="text-lg font-semibold">更换邮箱</h3>
    </template>

    <div class="space-y-4">
      <div class="text-muted text-sm">
        当前邮箱：<span class="font-medium text-default">{{ currentEmail || '未绑定' }}</span>
      </div>

      <UFormField label="新邮箱" required>
        <UInput
          v-model="emailState.newEmail"
          type="email"
          placeholder="请输入新邮箱地址"
          :disabled="loading || codeSent"
          class="w-full"
        />
      </UFormField>

      <div v-if="!codeSent" class="flex justify-end">
        <UButton
          :loading="loading"
          :disabled="!validNewEmail"
          @click="onSendCode"
        >
          发送验证码
        </UButton>
      </div>

      <template v-if="codeSent">
        <UFormField label="验证码" required>
          <div class="flex gap-2">
            <UInput
              v-model="emailState.code"
              placeholder="请输入收到的 6 位验证码"
              :disabled="loading"
              class="flex-1"
            />
            <UButton
              color="neutral"
              variant="soft"
              :disabled="countdown > 0 || loading"
              @click="onSendCode"
            >
              {{ countdown > 0 ? `${countdown}s 后重发` : '重新发送' }}
            </UButton>
          </div>
        </UFormField>

        <div class="flex justify-end">
          <UButton :loading="loading" :disabled="!emailState.code.trim()" @click="onEmailSubmit">
            确认更换
          </UButton>
        </div>
      </template>
    </div>
  </UCard>

  <UAlert
    v-if="emailSubmitError"
    color="error"
    variant="soft"
    :title="emailSubmitError"
  />
  <UAlert
    v-if="emailSubmitSuccess"
    color="success"
    variant="soft"
    :title="emailSubmitSuccess"
  />
</template>
