<script setup lang="ts">
/**
 * 产品 SKU 表单
 * 用于创建和编辑 SKU 骨架信息（不含定价/库存）
 */
const props = defineProps<{
  modelValue: Record<string, unknown>
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', v: Record<string, unknown>): void
}>()

const form = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
})

const statusOptions = [
  { label: '上架 (active)', value: 'active' },
  { label: '下架 (inactive)', value: 'inactive' },
  { label: '停售 (discontinued)', value: 'discontinued' }
]

// ============ 属性键值对编辑器 ============
interface KVPair { key: string; value: string }

const kvPairs = ref<KVPair[]>([])

/** 初始化时将 attributes 对象转换为 key-value 数组 */
watch(
  () => props.modelValue.attributes,
  (attrs) => {
    if (attrs && typeof attrs === 'object' && !Array.isArray(attrs)) {
      kvPairs.value = Object.entries(attrs as Record<string, unknown>).map(([k, v]) => ({
        key: k,
        value: String(v ?? '')
      }))
    } else {
      kvPairs.value = []
    }
  },
  { immediate: true }
)

/** 将 key-value 数组同步回 form.attributes */
function syncAttributes() {
  const attrs: Record<string, string> = {}
  for (const pair of kvPairs.value) {
    if (pair.key.trim()) {
      attrs[pair.key.trim()] = pair.value
    }
  }
  emit('update:modelValue', { ...props.modelValue, attributes: Object.keys(attrs).length ? attrs : undefined })
}

function addPair() {
  kvPairs.value.push({ key: '', value: '' })
}

function removePair(index: number) {
  kvPairs.value.splice(index, 1)
  syncAttributes()
}
</script>

<template>
  <div class="space-y-4">
    <!-- SKU 编码 -->
    <UFormField label="SKU 编码" required>
      <UInput
        :model-value="String(form.skuCode ?? '')"
        placeholder="如：PROD-001-RED-XL"
        class="w-full"
        @update:model-value="emit('update:modelValue', { ...form, skuCode: $event })"
      />
    </UFormField>

    <!-- SKU 名称 -->
    <UFormField label="SKU 名称" required>
      <UInput
        :model-value="String(form.name ?? '')"
        placeholder="如：红色 XL 码"
        class="w-full"
        @update:model-value="emit('update:modelValue', { ...form, name: $event })"
      />
    </UFormField>

    <!-- 描述 -->
    <UFormField label="描述">
      <UTextarea
        :model-value="String(form.description ?? '')"
        placeholder="可选描述"
        :rows="2"
        class="w-full"
        @update:model-value="emit('update:modelValue', { ...form, description: $event })"
      />
    </UFormField>

    <!-- 状态 -->
    <UFormField label="状态" required>
      <USelect
        :model-value="String(form.status ?? 'active')"
        :items="statusOptions"
        value-key="value"
        label-key="label"
        class="w-full"
        @update:model-value="emit('update:modelValue', { ...form, status: $event })"
      />
    </UFormField>

    <!-- 是否默认 -->
    <UFormField label="设为默认">
      <div class="flex items-center gap-2">
        <USwitch
          :model-value="Boolean(form.isDefault)"
          @update:model-value="emit('update:modelValue', { ...form, isDefault: $event })"
        />
        <span class="text-muted text-sm">{{ form.isDefault ? '是（默认展示该规格）' : '否' }}</span>
      </div>
    </UFormField>

    <!-- 规格属性（键值对） -->
    <UFormField label="规格属性">
      <div class="space-y-2">
        <div
          v-for="(pair, idx) in kvPairs"
          :key="idx"
          class="flex items-center gap-2"
        >
          <UInput
            v-model="pair.key"
            placeholder="属性名（如：颜色）"
            class="flex-1"
            @blur="syncAttributes"
          />
          <UInput
            v-model="pair.value"
            placeholder="属性值（如：红色）"
            class="flex-1"
            @blur="syncAttributes"
          />
          <UButton
            icon="i-lucide-x"
            color="error"
            variant="ghost"
            size="xs"
            @click="removePair(idx)"
          />
        </div>
        <UButton
          icon="i-lucide-plus"
          color="neutral"
          variant="outline"
          size="xs"
          @click="addPair"
        >
          添加属性
        </UButton>
      </div>
    </UFormField>

    <UDivider label="定价信息" class="my-2" />

    <!-- 原价 -->
    <UFormField label="原价" required>
      <UInput
        type="number"
        :model-value="form.originalPrice != null ? String(form.originalPrice) : ''"
        placeholder="如：99.00"
        class="w-full"
        @update:model-value="emit('update:modelValue', { ...form, originalPrice: $event !== '' ? Number($event) : undefined })"
      />
    </UFormField>

    <!-- 当前价格 -->
    <UFormField label="当前价格" hint="不填则与原价一致">
      <UInput
        type="number"
        :model-value="form.currentPrice != null ? String(form.currentPrice) : ''"
        placeholder="如：79.00"
        class="w-full"
        @update:model-value="emit('update:modelValue', { ...form, currentPrice: $event !== '' ? Number($event) : undefined })"
      />
    </UFormField>

    <!-- 货币 -->
    <UFormField label="货币">
      <USelect
        :model-value="String(form.currency ?? 'CNY')"
        :items="[{ label: '人民币 (CNY)', value: 'CNY' }, { label: '美元 (USD)', value: 'USD' }]"
        value-key="value"
        label-key="label"
        class="w-full"
        @update:model-value="emit('update:modelValue', { ...form, currency: $event })"
      />
    </UFormField>

    <!-- 库存类型 -->
    <UFormField label="库存类型">
      <USelect
        :model-value="String(form.inventoryType ?? 'unlimited')"
        :items="[
          { label: '无限库存 (unlimited)', value: 'unlimited' },
          { label: '有限库存 (limited)', value: 'limited' },
          { label: '预售 (preorder)', value: 'preorder' }
        ]"
        value-key="value"
        label-key="label"
        class="w-full"
        @update:model-value="emit('update:modelValue', { ...form, inventoryType: $event })"
      />
    </UFormField>
  </div>
</template>
