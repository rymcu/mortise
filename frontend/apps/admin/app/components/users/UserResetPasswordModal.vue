<script setup lang="ts">
/**
 * 重置用户密码弹窗
 */
const props = defineProps<{ user: Record<string, unknown> }>()
const emit = defineEmits<{ (e: 'success'): void }>()

const { loading, postAction } = useAdminCrud('/api/v1/admin/users')
const open = ref(false)
const newPassword = ref('')
const showNewPassword = ref(false)

const passwordRules = [
  { regex: /.{8,}/, text: '至少 8 个字符' },
  { regex: /\d/, text: '至少 1 个数字' },
  { regex: /[a-z]/, text: '至少 1 个小写字母' },
  { regex: /[A-Z]/, text: '至少 1 个大写字母' }
]

const strength = computed(() =>
  passwordRules.map((rule) => ({
    met: rule.regex.test(newPassword.value),
    text: rule.text
  }))
)

const score = computed(() => strength.value.filter((rule) => rule.met).length)

const strengthColor = computed(() => {
  if (score.value === 0) return 'neutral'
  if (score.value <= 1) return 'error'
  if (score.value <= 3) return 'warning'
  return 'success'
})

const strengthText = computed(() => {
  if (score.value === 0) return '请输入新密码'
  if (score.value <= 2) return '密码强度弱'
  if (score.value === 3) return '密码强度中'
  return '密码强度高'
})

const canSubmit = computed(() => score.value === 4)

async function onConfirm() {
  if (!canSubmit.value) return
  const result = await postAction(`/${props.user.id}/reset-password`, {
    password: newPassword.value
  })
  if (result !== null) {
    open.value = false
    newPassword.value = ''
    showNewPassword.value = false
    emit('success')
  }
}
</script>

<template>
  <UModal v-model:open="open" title="重置密码">
    <UButton
      icon="i-lucide-key-round"
      color="neutral"
      variant="ghost"
      size="xs"
      @click="open = true"
    >
      重置密码
    </UButton>
    <template #body>
      <div class="space-y-3">
        <p class="text-muted text-sm">
          为用户「{{ user.nickname || user.account }}」设置新密码
        </p>
        <UInput
          v-model="newPassword"
          :color="strengthColor"
          :type="showNewPassword ? 'text' : 'password'"
          placeholder="请输入新密码"
          :aria-invalid="score < 4"
          aria-describedby="reset-password-strength"
          :ui="{ trailing: 'pe-1' }"
          class="w-full"
        >
          <template #trailing>
            <UButton
              color="neutral"
              variant="link"
              size="sm"
              :icon="showNewPassword ? 'i-lucide-eye-off' : 'i-lucide-eye'"
              :aria-label="showNewPassword ? '隐藏密码' : '显示密码'"
              :aria-pressed="showNewPassword"
              @click="showNewPassword = !showNewPassword"
            />
          </template>
        </UInput>

        <UProgress
          :color="strengthColor"
          :indicator="strengthText"
          :model-value="score"
          :max="4"
          size="sm"
        />

        <p id="reset-password-strength" class="text-muted text-sm">
          {{ strengthText }}，密码需满足：
        </p>

        <ul class="space-y-1" aria-label="密码强度要求">
          <li
            v-for="(rule, index) in strength"
            :key="index"
            class="flex items-center gap-1"
            :class="rule.met ? 'text-success' : 'text-muted'"
          >
            <UIcon
              :name="rule.met ? 'i-lucide-circle-check' : 'i-lucide-circle-x'"
              class="size-4 shrink-0"
            />
            <span class="text-xs">{{ rule.text }}</span>
          </li>
        </ul>
      </div>
    </template>
    <template #footer>
      <div class="flex w-full justify-end gap-2">
        <UButton
          color="primary"
          :loading="loading"
          :disabled="!canSubmit"
          @click="onConfirm"
          >确认重置</UButton
        >
        <UButton
          color="neutral"
          variant="subtle"
          :disabled="loading"
          @click="open = false"
          >取消</UButton
        >
      </div>
    </template>
  </UModal>
</template>
