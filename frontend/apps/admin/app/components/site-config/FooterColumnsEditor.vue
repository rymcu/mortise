<script setup lang="ts">
import type { FormFieldDef } from '~/types'
import type { FooterColumnEditorValue, FooterColumnLinkEditorValue } from '~/types/site-config'
import {
  createEmptyFooterColumn,
  createEmptyFooterColumnLink,
  FOOTER_LINK_TARGET_OPTIONS,
  parseFooterColumnsEditorValue,
  serializeFooterColumnsEditorValue
} from '~/utils/footer-columns'

const props = defineProps<{
  field: FormFieldDef
  modelValue: string | undefined
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const columns = ref<FooterColumnEditorValue[]>([])
const hasInvalidInput = ref(false)
const syncedValue = ref('')

watch(
  () => props.modelValue,
  (value) => {
    const normalized = value ?? ''

    if (normalized === syncedValue.value) {
      return
    }

    const parsed = parseFooterColumnsEditorValue(value)
    columns.value = parsed.columns
    hasInvalidInput.value = parsed.hasInvalidInput
    syncedValue.value = normalized
  },
  { immediate: true }
)

function syncModelValue() {
  const serialized = serializeFooterColumnsEditorValue(columns.value)
  syncedValue.value = serialized
  hasInvalidInput.value = false
  emit('update:modelValue', serialized)
}

function updateColumn(columnIndex: number, patch: Partial<FooterColumnEditorValue>) {
  const column = columns.value[columnIndex]

  if (!column) {
    return
  }

  columns.value[columnIndex] = {
    ...column,
    ...patch
  }
  syncModelValue()
}

function moveColumn(columnIndex: number, direction: -1 | 1) {
  const targetIndex = columnIndex + direction

  if (targetIndex < 0 || targetIndex >= columns.value.length) {
    return
  }

  const [column] = columns.value.splice(columnIndex, 1)

  if (!column) {
    return
  }

  columns.value.splice(targetIndex, 0, column)
  syncModelValue()
}

function removeColumn(columnIndex: number) {
  columns.value.splice(columnIndex, 1)
  syncModelValue()
}

function addColumn() {
  columns.value.push(createEmptyFooterColumn())
  syncModelValue()
}

function updateLink(columnIndex: number, linkIndex: number, patch: Partial<FooterColumnLinkEditorValue>) {
  const column = columns.value[columnIndex]
  const link = column?.children[linkIndex]

  if (!column || !link) {
    return
  }

  column.children[linkIndex] = {
    ...link,
    ...patch
  }
  syncModelValue()
}

function moveLink(columnIndex: number, linkIndex: number, direction: -1 | 1) {
  const column = columns.value[columnIndex]

  if (!column) {
    return
  }

  const targetIndex = linkIndex + direction

  if (targetIndex < 0 || targetIndex >= column.children.length) {
    return
  }

  const [link] = column.children.splice(linkIndex, 1)

  if (!link) {
    return
  }

  column.children.splice(targetIndex, 0, link)
  syncModelValue()
}

function removeLink(columnIndex: number, linkIndex: number) {
  const column = columns.value[columnIndex]

  if (!column) {
    return
  }

  column.children.splice(linkIndex, 1)
  syncModelValue()
}

function addLink(columnIndex: number) {
  const column = columns.value[columnIndex]

  if (!column) {
    return
  }

  column.children.push(createEmptyFooterColumnLink())
  syncModelValue()
}
</script>

<template>
  <UFormField :label="field.label" :required="field.required">
    <div class="space-y-4">
      <UAlert
        v-if="hasInvalidInput"
        color="warning"
        variant="soft"
        title="当前配置不是合法的页脚栏目 JSON"
        description="已按空配置展示。重新编辑并保存后，会覆盖为新的结构化值。"
      />

      <UAlert
        v-else-if="!columns.length"
        color="neutral"
        variant="soft"
        title="尚未配置页脚栏目"
        description="点击“添加栏目”开始编辑。留空时前台会回退到默认页脚栏目。"
      />

      <div
        v-for="(column, columnIndex) in columns"
        :key="column.id"
        class="space-y-3 rounded-xl border border-default p-4"
      >
        <div class="flex flex-wrap items-center justify-between gap-2">
          <p class="font-medium text-sm">
            栏目 {{ columnIndex + 1 }}
          </p>
          <div class="flex items-center gap-1">
            <UButton
              type="button"
              color="neutral"
              variant="ghost"
              size="xs"
              icon="i-lucide-chevron-up"
              :disabled="columnIndex === 0"
              aria-label="上移栏目"
              @click="moveColumn(columnIndex, -1)"
            />
            <UButton
              type="button"
              color="neutral"
              variant="ghost"
              size="xs"
              icon="i-lucide-chevron-down"
              :disabled="columnIndex === columns.length - 1"
              aria-label="下移栏目"
              @click="moveColumn(columnIndex, 1)"
            />
            <UButton
              type="button"
              color="error"
              variant="ghost"
              size="xs"
              icon="i-lucide-trash-2"
              aria-label="删除栏目"
              @click="removeColumn(columnIndex)"
            />
          </div>
        </div>

        <UFormField label="栏目标题">
          <UInput
            :model-value="column.label"
            placeholder="如：资源"
            class="w-full"
            @update:model-value="updateColumn(columnIndex, { label: $event })"
          />
        </UFormField>

        <div class="space-y-3">
          <div class="flex items-center justify-between gap-2">
            <p class="font-medium text-sm">
              栏目链接
            </p>
            <UButton
              type="button"
              icon="i-lucide-plus"
              color="neutral"
              variant="outline"
              size="xs"
              @click="addLink(columnIndex)"
            >
              添加链接
            </UButton>
          </div>

          <div
            v-if="column.children.length"
            class="space-y-3"
          >
            <div
              v-for="(link, linkIndex) in column.children"
              :key="link.id"
              class="space-y-3 rounded-lg border border-dashed border-default p-3"
            >
              <div class="flex flex-wrap items-center justify-between gap-2">
                <p class="text-sm text-toned">
                  链接 {{ linkIndex + 1 }}
                </p>
                <div class="flex items-center gap-1">
                  <UButton
                    type="button"
                    color="neutral"
                    variant="ghost"
                    size="xs"
                    icon="i-lucide-chevron-up"
                    :disabled="linkIndex === 0"
                    aria-label="上移链接"
                    @click="moveLink(columnIndex, linkIndex, -1)"
                  />
                  <UButton
                    type="button"
                    color="neutral"
                    variant="ghost"
                    size="xs"
                    icon="i-lucide-chevron-down"
                    :disabled="linkIndex === column.children.length - 1"
                    aria-label="下移链接"
                    @click="moveLink(columnIndex, linkIndex, 1)"
                  />
                  <UButton
                    type="button"
                    color="error"
                    variant="ghost"
                    size="xs"
                    icon="i-lucide-trash-2"
                    aria-label="删除链接"
                    @click="removeLink(columnIndex, linkIndex)"
                  />
                </div>
              </div>

              <div class="grid gap-3 md:grid-cols-2">
                <UFormField label="链接名称">
                  <UInput
                    :model-value="link.label"
                    placeholder="如：文档"
                    class="w-full"
                    @update:model-value="updateLink(columnIndex, linkIndex, { label: $event })"
                  />
                </UFormField>

                <UFormField label="链接地址">
                  <UInput
                    :model-value="link.to"
                    placeholder="如：/docs 或 https://example.com"
                    class="w-full"
                    @update:model-value="updateLink(columnIndex, linkIndex, { to: $event })"
                  />
                </UFormField>
              </div>

              <UFormField label="打开方式">
                <USelect
                  :model-value="link.target || undefined"
                  :items="FOOTER_LINK_TARGET_OPTIONS"
                  value-key="value"
                  placeholder="当前窗口"
                  class="w-full"
                  @update:model-value="updateLink(columnIndex, linkIndex, { target: String($event ?? '') })"
                />
              </UFormField>
            </div>
          </div>

          <p
            v-else
            class="text-sm text-muted"
          >
            当前栏目还没有链接，点击“添加链接”开始配置。
          </p>
        </div>
      </div>

      <div class="flex flex-wrap items-center gap-2">
        <UButton
          type="button"
          icon="i-lucide-plus"
          color="neutral"
          variant="outline"
          @click="addColumn"
        >
          添加栏目
        </UButton>
        <span class="text-xs text-muted">
          留空的栏目或链接在保存时会自动忽略。
        </span>
      </div>
    </div>
  </UFormField>
</template>
