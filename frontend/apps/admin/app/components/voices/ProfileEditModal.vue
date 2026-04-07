<script setup lang="ts">
import { toVoiceProfilePayload } from '~/types/voice'
import type {
  VoiceModelInfo,
  VoiceProfileFormState,
  VoiceProfileInfo,
  VoiceSelectOption,
} from '~/types/voice'

const open = defineModel<boolean>('open', { default: false })

const props = withDefaults(
  defineProps<{
    profile?: VoiceProfileInfo | null
    providerOptions: VoiceSelectOption[]
    models: VoiceModelInfo[]
  }>(),
  {
    profile: null,
  }
)

const emit = defineEmits<{
  (e: 'success'): void
}>()

const { update, loading, errorMessage } = useAdminCrud('/api/v1/admin/voice/profiles')

const formRef = ref()
const formData = ref<VoiceProfileFormState | null>(null)

async function handleConfirm() {
  if (!props.profile?.id) {
    return
  }

  const valid = await formRef.value?.validate()
  if (!valid || !formData.value) {
    return
  }

  const ok = await update(props.profile.id, toVoiceProfilePayload(formData.value))
  if (ok) {
    open.value = false
    emit('success')
  }
}
</script>

<template>
  <UModal v-model:open="open" title="编辑语音 Profile" :ui="{ content: 'sm:max-w-5xl' }">
    <template #body>
      <UAlert
        v-if="errorMessage"
        color="error"
        :title="errorMessage"
        class="mb-4"
      />
      <VoicesProfileForm
        ref="formRef"
        :data="profile"
        :provider-options="providerOptions"
        :models="models"
        @change="formData = $event"
      />
    </template>

    <template #footer>
      <div class="flex justify-end gap-2">
        <UButton variant="ghost" label="取消" @click="open = false" />
        <UButton label="保存" :loading="loading" @click="handleConfirm" />
      </div>
    </template>
  </UModal>
</template>