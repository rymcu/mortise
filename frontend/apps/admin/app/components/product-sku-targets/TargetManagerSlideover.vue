<script setup lang="ts">
import {
  fetchAdminDelete,
  fetchAdminGet,
  fetchAdminPost,
  fetchAdminPut,
} from '@mortise/core-sdk'
import type {
  ProductSkuInfo,
  ProductSkuTargetFormState,
  ProductSkuTargetInfo,
} from '~/types/migration'
import { productTargetStatusOptions } from '~/types/migration'
import {
  isRecord,
  jsonTextFromValue,
  parseJsonObjectText,
  toOptionalNumber,
  toOptionalString,
} from '~/utils/migration'

const open = defineModel<boolean>('open', { default: false })

const props = withDefaults(
  defineProps<{
    productId: string
    sku?: ProductSkuInfo | null
  }>(),
  {
    sku: null,
  }
)

const toast = useToast()
const { $api } = useNuxtApp()

const loading = ref(false)
const errorMessage = ref('')
const targets = ref<ProductSkuTargetInfo[]>([])

const formModalOpen = ref(false)
const editingTarget = ref<ProductSkuTargetInfo | null>(null)
const formErrorMessage = ref('')
const formLoading = ref(false)
const deleteModalOpen = ref(false)
const deleteTarget = ref<ProductSkuTargetInfo | null>(null)

const form = reactive<ProductSkuTargetFormState>(createFormState())

function resourcePath(skuId: string) {
  return `/api/v1/admin/products/${props.productId}/skus/${skuId}/targets`
}

function normalizeTarget(value: unknown): ProductSkuTargetInfo | null {
  if (!isRecord(value)) {
    return null
  }

  const id = toOptionalString(value.id)
  if (!id) {
    return null
  }

  return {
    id,
    productSkuId: toOptionalString(value.productSkuId),
    targetType: toOptionalString(value.targetType),
    targetId: toOptionalString(value.targetId),
    quantity: toOptionalNumber(value.quantity),
    validityDays: toOptionalNumber(value.validityDays),
    accessLevel: toOptionalString(value.accessLevel),
    conditions: isRecord(value.conditions) ? value.conditions : undefined,
    metadata: isRecord(value.metadata) ? value.metadata : undefined,
    status: toOptionalNumber(value.status),
    createdTime: toOptionalString(value.createdTime),
    updatedTime: toOptionalString(value.updatedTime),
  }
}

function normalizeTargets(value: unknown): ProductSkuTargetInfo[] {
  if (!Array.isArray(value)) {
    return []
  }

  return value
    .map(item => normalizeTarget(item))
    .filter((item): item is ProductSkuTargetInfo => !!item)
}

function createFormState(data?: ProductSkuTargetInfo | null): ProductSkuTargetFormState {
  return {
    targetType: data?.targetType ?? '',
    targetId: data?.targetId ?? '',
    quantity: data?.quantity ?? 1,
    validityDays: data?.validityDays ?? null,
    accessLevel: data?.accessLevel ?? '',
    status: data?.status ?? 1,
    conditions: jsonTextFromValue(data?.conditions),
    metadata: jsonTextFromValue(data?.metadata),
  }
}

function resetForm() {
  Object.assign(form, createFormState(editingTarget.value))
  formErrorMessage.value = ''
}

watch(open, async (value) => {
  if (value) {
    await loadTargets()
  }
})

watch(() => props.sku?.id, async () => {
  if (open.value) {
    await loadTargets()
  }
})

async function loadTargets() {
  if (!props.sku?.id) {
    targets.value = []
    return
  }

  loading.value = true
  errorMessage.value = ''
  try {
    const data = await fetchAdminGet<unknown[]>($api, resourcePath(props.sku.id))
    targets.value = normalizeTargets(data)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '加载目标映射失败'
  } finally {
    loading.value = false
  }
}

function openCreateForm() {
  editingTarget.value = null
  resetForm()
  formModalOpen.value = true
}

function openEditForm(target: ProductSkuTargetInfo) {
  editingTarget.value = target
  resetForm()
  formModalOpen.value = true
}

function buildPayload() {
  const conditionsResult = parseJsonObjectText(form.conditions)
  if (!conditionsResult.success) {
    throw new Error(`conditions ${conditionsResult.error}`)
  }

  const metadataResult = parseJsonObjectText(form.metadata)
  if (!metadataResult.success) {
    throw new Error(`metadata ${metadataResult.error}`)
  }

  if (!form.targetType.trim()) {
    throw new Error('请输入目标类型')
  }

  if (!form.targetId.trim()) {
    throw new Error('请输入目标 ID')
  }

  if (!Number.isFinite(form.quantity) || form.quantity <= 0) {
    throw new Error('发放数量必须大于 0')
  }

  return {
    targetType: form.targetType.trim(),
    targetId: form.targetId.trim(),
    quantity: form.quantity,
    validityDays: form.validityDays,
    accessLevel: form.accessLevel.trim() || null,
    status: form.status,
    conditions: conditionsResult.data,
    metadata: metadataResult.data,
  }
}

async function submitForm() {
  if (!props.sku?.id) {
    return
  }

  formLoading.value = true
  formErrorMessage.value = ''

  try {
    const payload = buildPayload()
    if (editingTarget.value?.id) {
      await fetchAdminPut<boolean>(
        $api,
        `${resourcePath(props.sku.id)}/${editingTarget.value.id}`,
        payload
      )
      toast.add({ title: '目标映射已更新', color: 'success' })
    } else {
      await fetchAdminPost<boolean>(
        $api,
        resourcePath(props.sku.id),
        payload
      )
      toast.add({ title: '目标映射已创建', color: 'success' })
    }

    formModalOpen.value = false
    await loadTargets()
  } catch (error) {
    formErrorMessage.value = error instanceof Error ? error.message : '保存目标映射失败'
  } finally {
    formLoading.value = false
  }
}

function askDelete(target: ProductSkuTargetInfo) {
  deleteTarget.value = target
  deleteModalOpen.value = true
}

async function confirmDelete() {
  if (!props.sku?.id || !deleteTarget.value?.id) {
    return
  }

  formLoading.value = true
  try {
    await fetchAdminDelete(
      $api,
      `${resourcePath(props.sku.id)}/${deleteTarget.value.id}`
    )
    toast.add({ title: '目标映射已删除', color: 'success' })
    deleteModalOpen.value = false
    await loadTargets()
  } catch (error) {
    toast.add({
      title: '删除目标映射失败',
      description: error instanceof Error ? error.message : '请稍后重试',
      color: 'error',
    })
  } finally {
    formLoading.value = false
  }
}
</script>

<template>
  <USlideover
    v-model:open="open"
    :title="`目标映射 - ${sku?.name || sku?.skuCode || sku?.id || '-'}`"
    :ui="{ content: 'sm:max-w-4xl' }"
  >
    <template #body>
      <div class="space-y-4 p-1">
        <UAlert
          color="primary"
          variant="soft"
          title="SKU 目标映射"
          description="这里维护当前 SKU 对应的业务目标、数量、有效期和 JSON 条件。"
        />

        <UAlert
          v-if="errorMessage"
          color="error"
          variant="soft"
          :title="errorMessage"
        />

        <div class="flex items-center justify-between gap-2">
          <div class="text-sm text-muted">
            当前 SKU：{{ sku?.skuCode || sku?.id || '-' }}
          </div>
          <div class="flex items-center gap-2">
            <UButton color="neutral" variant="soft" :loading="loading" @click="loadTargets">
              刷新
            </UButton>
            <UButton icon="i-lucide-plus" color="primary" @click="openCreateForm">
              新增映射
            </UButton>
          </div>
        </div>

        <div class="space-y-3">
          <UCard v-for="target in targets" :key="target.id">
            <div class="flex flex-wrap items-start justify-between gap-3">
              <div class="space-y-2">
                <div class="flex flex-wrap items-center gap-2">
                  <p class="font-medium">{{ target.targetType || '-' }}</p>
                  <UBadge :color="target.status === 1 ? 'success' : 'neutral'" variant="subtle" size="sm">
                    {{ target.status === 1 ? '启用' : '禁用' }}
                  </UBadge>
                </div>
                <div class="grid gap-2 text-sm text-muted md:grid-cols-3">
                  <p>目标 ID：{{ target.targetId || '-' }}</p>
                  <p>数量：{{ target.quantity ?? '-' }}</p>
                  <p>有效期：{{ target.validityDays ?? '长期' }}</p>
                  <p>权限：{{ target.accessLevel || '-' }}</p>
                  <p>更新时间：{{ target.updatedTime || '-' }}</p>
                </div>
              </div>

              <div class="flex items-center gap-1">
                <UButton icon="i-lucide-pencil" color="neutral" variant="ghost" size="xs" @click="openEditForm(target)" />
                <UButton icon="i-lucide-trash-2" color="error" variant="ghost" size="xs" @click="askDelete(target)" />
              </div>
            </div>

            <div class="mt-3 grid gap-3 md:grid-cols-2">
              <div class="rounded-lg border border-default p-3">
                <p class="mb-2 text-xs font-medium text-muted">Conditions</p>
                <pre class="overflow-auto text-xs">{{ target.conditions ? JSON.stringify(target.conditions, null, 2) : '{}' }}</pre>
              </div>
              <div class="rounded-lg border border-default p-3">
                <p class="mb-2 text-xs font-medium text-muted">Metadata</p>
                <pre class="overflow-auto text-xs">{{ target.metadata ? JSON.stringify(target.metadata, null, 2) : '{}' }}</pre>
              </div>
            </div>
          </UCard>

          <div
            v-if="!targets.length && !loading"
            class="rounded-xl border border-dashed border-default p-8 text-center text-sm text-muted"
          >
            当前 SKU 暂无目标映射
          </div>
        </div>
      </div>
    </template>

    <UModal v-model:open="formModalOpen" :title="editingTarget ? '编辑目标映射' : '新增目标映射'" :ui="{ content: 'sm:max-w-3xl' }">
      <template #body>
        <div class="space-y-4">
          <UAlert
            v-if="formErrorMessage"
            color="error"
            variant="soft"
            :title="formErrorMessage"
          />

          <UForm :state="form" class="space-y-4">
            <div class="grid gap-4 md:grid-cols-2">
              <UFormField label="目标类型" required>
                <UInput v-model="form.targetType" class="w-full" placeholder="例如 COURSE / SCHEDULE / LESSON" />
              </UFormField>

              <UFormField label="目标 ID" required>
                <UInput v-model="form.targetId" class="w-full" placeholder="请输入目标实体 ID" />
              </UFormField>
            </div>

            <div class="grid gap-4 md:grid-cols-4">
              <UFormField label="数量" required>
                <UInput v-model.number="form.quantity" type="number" class="w-full" />
              </UFormField>

              <UFormField label="有效期（天）">
                <UInput v-model.number="form.validityDays" type="number" class="w-full" />
              </UFormField>

              <UFormField label="访问等级">
                <UInput v-model="form.accessLevel" class="w-full" placeholder="如 VIP / STANDARD" />
              </UFormField>

              <UFormField label="状态">
                <USelect
                  :model-value="String(form.status)"
                  :items="productTargetStatusOptions"
                  value-key="value"
                  label-key="label"
                  class="w-full"
                  @update:model-value="form.status = Number($event)"
                />
              </UFormField>
            </div>

            <div class="grid gap-4 md:grid-cols-2">
              <UFormField label="Conditions JSON">
                <UTextarea
                  v-model="form.conditions"
                  class="w-full font-mono"
                  :rows="8"
                  placeholder="{&#10;  &quot;needActivation&quot;: true&#10;}"
                />
              </UFormField>

              <UFormField label="Metadata JSON">
                <UTextarea
                  v-model="form.metadata"
                  class="w-full font-mono"
                  :rows="8"
                  placeholder="{&#10;  &quot;source&quot;: &quot;admin&quot;&#10;}"
                />
              </UFormField>
            </div>
          </UForm>
        </div>
      </template>

      <template #footer>
        <div class="flex w-full justify-end gap-2">
          <UButton color="neutral" variant="ghost" :disabled="formLoading" @click="formModalOpen = false">
            取消
          </UButton>
          <UButton color="primary" :loading="formLoading" @click="submitForm">
            保存
          </UButton>
        </div>
      </template>
    </UModal>

    <AdminConfirmDeleteModal
      v-model:open="deleteModalOpen"
      title="确认删除目标映射"
      :loading="formLoading"
      :message="`确认删除“${deleteTarget?.targetType || '-'} / ${deleteTarget?.targetId || '-'}”吗？`"
      @confirm="confirmDelete"
    />
  </USlideover>
</template>
