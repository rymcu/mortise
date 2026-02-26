<script setup lang="ts">
/**
 * 安全设置页面
 */
const state = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const show = reactive({
  currentPassword: false,
  newPassword: false,
  confirmPassword: false
})

const submitError = ref('')
const submitSuccess = ref('')

const rules = [
  { regex: /.{8,}/, text: '至少 8 个字符' },
  { regex: /\d/, text: '至少 1 个数字' },
  { regex: /[a-z]/, text: '至少 1 个小写字母' },
  { regex: /[A-Z]/, text: '至少 1 个大写字母' }
]

const strength = computed(() =>
  rules.map((rule) => ({
    met: rule.regex.test(state.newPassword),
    text: rule.text
  }))
)

const score = computed(() => strength.value.filter((rule) => rule.met).length)

const color = computed(() => {
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

const confirmMatched = computed(
  () =>
    state.confirmPassword.length > 0 &&
    state.newPassword === state.confirmPassword
)
const canSubmit = computed(
  () =>
    state.currentPassword.length > 0 &&
    score.value === 4 &&
    confirmMatched.value
)

function onSubmit() {
  submitError.value = ''
  submitSuccess.value = ''

  if (!canSubmit.value) {
    submitError.value = '请先满足密码强度要求，并确认两次输入的新密码一致'
    return
  }

  submitSuccess.value =
    '前端校验通过：当前版本待后端“当前用户改密接口”接入后可直接提交'
}
</script>

<template>
  <UDashboardPanel id="settings-security">
    <template #header>
      <UDashboardNavbar title="安全设置">
        <template #leading>
          <UDashboardSidebarCollapse />
        </template>
      </UDashboardNavbar>
    </template>

    <template #body>
      <div class="mx-auto max-w-2xl space-y-6">
        <UCard>
          <template #header>
            <h3 class="text-lg font-semibold">修改密码</h3>
          </template>

          <div class="space-y-4">
            <UFormField label="当前密码" required>
              <UInput
                v-model="state.currentPassword"
                :type="show.currentPassword ? 'text' : 'password'"
                placeholder="请输入当前密码"
                :ui="{ trailing: 'pe-1' }"
                class="w-full"
              >
                <template #trailing>
                  <UButton
                    color="neutral"
                    variant="link"
                    size="sm"
                    :icon="
                      show.currentPassword ? 'i-lucide-eye-off' : 'i-lucide-eye'
                    "
                    :aria-label="show.currentPassword ? '隐藏密码' : '显示密码'"
                    :aria-pressed="show.currentPassword"
                    @click="show.currentPassword = !show.currentPassword"
                  />
                </template>
              </UInput>
            </UFormField>

            <UFormField label="新密码" required>
              <UInput
                v-model="state.newPassword"
                :color="color"
                :type="show.newPassword ? 'text' : 'password'"
                :aria-invalid="score < 4"
                aria-describedby="new-password-strength"
                placeholder="请输入新密码"
                :ui="{ trailing: 'pe-1' }"
                class="w-full"
              >
                <template #trailing>
                  <UButton
                    color="neutral"
                    variant="link"
                    size="sm"
                    :icon="
                      show.newPassword ? 'i-lucide-eye-off' : 'i-lucide-eye'
                    "
                    :aria-label="show.newPassword ? '隐藏密码' : '显示密码'"
                    :aria-pressed="show.newPassword"
                    @click="show.newPassword = !show.newPassword"
                  />
                </template>
              </UInput>
            </UFormField>

            <UProgress
              :color="color"
              :indicator="strengthText"
              :model-value="score"
              :max="4"
              size="sm"
            />

            <p id="new-password-strength" class="text-muted text-sm">
              {{ strengthText }}，新密码需满足：
            </p>

            <ul class="space-y-1" aria-label="密码强度要求">
              <li
                v-for="(rule, index) in strength"
                :key="index"
                class="flex items-center gap-1"
                :class="rule.met ? 'text-success' : 'text-muted'"
              >
                <UIcon
                  :name="
                    rule.met ? 'i-lucide-circle-check' : 'i-lucide-circle-x'
                  "
                  class="size-4 shrink-0"
                />
                <span class="text-xs">{{ rule.text }}</span>
              </li>
            </ul>

            <UFormField label="确认新密码" required>
              <UInput
                v-model="state.confirmPassword"
                :type="show.confirmPassword ? 'text' : 'password'"
                :color="
                  state.confirmPassword
                    ? confirmMatched
                      ? 'success'
                      : 'error'
                    : 'neutral'
                "
                :aria-invalid="
                  state.confirmPassword.length > 0 && !confirmMatched
                "
                placeholder="请再次输入新密码"
                :ui="{ trailing: 'pe-1' }"
                class="w-full"
              >
                <template #trailing>
                  <UButton
                    color="neutral"
                    variant="link"
                    size="sm"
                    :icon="
                      show.confirmPassword ? 'i-lucide-eye-off' : 'i-lucide-eye'
                    "
                    :aria-label="show.confirmPassword ? '隐藏密码' : '显示密码'"
                    :aria-pressed="show.confirmPassword"
                    @click="show.confirmPassword = !show.confirmPassword"
                  />
                </template>
              </UInput>
            </UFormField>

            <div class="flex justify-end">
              <UButton :disabled="!canSubmit" @click="onSubmit">
                提交修改
              </UButton>
            </div>
          </div>
        </UCard>

        <UAlert
          v-if="submitError"
          color="error"
          variant="soft"
          :title="submitError"
        />
        <UAlert
          v-if="submitSuccess"
          color="success"
          variant="soft"
          :title="submitSuccess"
        />
      </div>
    </template>
  </UDashboardPanel>
</template>
