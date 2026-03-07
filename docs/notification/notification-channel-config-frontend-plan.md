# 通知渠道配置管理 - 前端实现计划

## 概览

在管理端新增「通知渠道配置」页面，允许管理员通过 Tab 切换不同渠道（邮件、短信、微信等），查看并编辑各渠道的配置参数，无需修改配置文件或重启服务。

---

## 一、API 接口说明

| 方法   | 路径                                     | 说明                                   |
|--------|------------------------------------------|----------------------------------------|
| GET    | `/api/v1/admin/notification/channels`   | 获取所有渠道配置（含 schema + values） |
| GET    | `/api/v1/admin/notification/channels/:channel` | 获取单个渠道配置                |
| PUT    | `/api/v1/admin/notification/channels/:channel` | 保存单个渠道配置                |

### GET 响应结构（`ChannelConfigVO[]`）

```json
[
  {
    "channel": "email",
    "label": "邮件通知",
    "enabled": true,
    "schema": [
      { "key": "host",     "label": "SMTP 服务器",  "type": "TEXT",     "required": true,  "placeholder": "smtp.example.com", "defaultValue": null, "options": null },
      { "key": "port",     "label": "端口",          "type": "NUMBER",   "required": true,  "placeholder": "465",              "defaultValue": "465","options": null },
      { "key": "username", "label": "发件邮箱",      "type": "EMAIL",    "required": true,  "placeholder": "no-reply@...",     "defaultValue": null, "options": null },
      { "key": "password", "label": "授权码/密码",   "type": "PASSWORD", "required": true,  "placeholder": "",                 "defaultValue": null, "options": null },
      { "key": "ssl",      "label": "启用 SSL",      "type": "BOOLEAN",  "required": false, "placeholder": null,               "defaultValue": "true","options": null },
      { "key": "from_name","label": "发件人名称",    "type": "TEXT",     "required": false, "placeholder": "系统通知",          "defaultValue": null, "options": null }
    ],
    "values": {
      "host": "smtp.qq.com",
      "port": "465",
      "username": "no-reply@example.com",
      "password": "***",
      "ssl": "true",
      "from_name": "系统通知"
    }
  },
  {
    "channel": "sms",
    "label": "短信通知",
    "enabled": false,
    "schema": [
      { "key": "provider",   "label": "短信供应商", "type": "SELECT", "required": true, "options": [{"label":"阿里云","value":"aliyun"},{"label":"腾讯云","value":"tencent"}] },
      { "key": "access_key", "label": "AccessKey",  "type": "TEXT",   "required": true  },
      { "key": "secret_key", "label": "SecretKey",  "type": "PASSWORD","required": true  },
      { "key": "sign_name",  "label": "短信签名",   "type": "TEXT",   "required": true  }
    ],
    "values": {}
  }
]
```

### PUT 请求体（`ChannelConfigSaveRequest`）

```json
{
  "enabled": true,
  "values": {
    "host": "smtp.qq.com",
    "port": "465",
    "username": "no-reply@example.com",
    "password": "newpassword123",
    "ssl": "true",
    "from_name": "系统通知"
  }
}
```

> **密码脱敏规则**：若用户未修改密码字段，应将 `"***"` 替换为空字符串或由后端识别 `"***"` 视为"不修改"。
> 推荐方案：前端若值为 `"***"` 则从 values 中删除该 key，后端 `saveChannel` 判断 key 缺失时跳过更新。

---

## 二、文件结构

```
frontend/apps/admin/app/
├── pages/systems/
│   └── notification-channels.vue          # 页面（Tab 切换 + 渠道卡片）
├── components/notification-channels/
│   ├── ChannelConfigCard.vue              # 单个渠道配置卡片（含表单 + 保存按钮）
│   └── ChannelDynamicField.vue           # 动态字段渲染组件（按 type 渲染控件）
└── composables/
    └── useNotificationChannels.ts         # 数据请求 + 保存逻辑封装
```

---

## 三、类型定义

在项目 `types/index.d.ts` 中追加：

```typescript
/** 表单字段 UI 渲染类型（与后端 FormFieldType 对应） */
type FormFieldType = 'TEXT' | 'PASSWORD' | 'NUMBER' | 'BOOLEAN' | 'EMAIL' | 'SELECT'

interface ChannelFieldOption {
  label: string
  value: string
}

interface ChannelFieldDef {
  key: string
  label: string
  type: FormFieldType
  required: boolean
  placeholder?: string
  defaultValue?: string
  options?: ChannelFieldOption[]
}

interface ChannelConfigVO {
  channel: string
  label: string
  enabled: boolean
  schema: ChannelFieldDef[]
  values: Record<string, string>
}

interface ChannelConfigSaveRequest {
  enabled: boolean
  values: Record<string, string>
}
```

---

## 四、Composable — `useNotificationChannels.ts`

```typescript
// composables/useNotificationChannels.ts
export function useNotificationChannels() {
  const { $api } = useNuxtApp()
  const channels = ref<ChannelConfigVO[]>([])
  const loading = ref(false)
  const saving = ref(false)
  const errorMessage = ref('')

  async function loadChannels() {
    loading.value = true
    errorMessage.value = ''
    try {
      const result = await $api<{ data: ChannelConfigVO[] }>(
        '/api/v1/admin/notification/channels'
      )
      channels.value = result.data ?? []
    } catch (e) {
      errorMessage.value = '加载通知渠道配置失败'
    } finally {
      loading.value = false
    }
  }

  async function saveChannel(channel: string, request: ChannelConfigSaveRequest) {
    saving.value = true
    errorMessage.value = ''
    try {
      await $api(`/api/v1/admin/notification/channels/${channel}`, {
        method: 'PUT',
        body: request
      })
      // 更新本地状态
      const target = channels.value.find(c => c.channel === channel)
      if (target) {
        target.enabled = request.enabled
        // 密码字段保留脱敏显示
        Object.entries(request.values).forEach(([k, v]) => {
          target.values[k] = target.schema.find(f => f.key === k)?.type === 'PASSWORD' ? '***' : v
        })
      }
      return true
    } catch (e) {
      errorMessage.value = '保存失败，请检查配置后重试'
      return false
    } finally {
      saving.value = false
    }
  }

  return { channels, loading, saving, errorMessage, loadChannels, saveChannel }
}
```

---

## 五、组件实现

### 5.1 `ChannelDynamicField.vue` — 动态字段渲染

```vue
<!-- components/notification-channels/ChannelDynamicField.vue -->
<script setup lang="ts">
defineProps<{
  field: ChannelFieldDef
  modelValue: string | undefined
}>()
defineEmits<{ 'update:modelValue': [value: string] }>()
</script>

<template>
  <UFormField :label="field.label" :required="field.required">
    <!-- 布尔开关 -->
    <USwitch
      v-if="field.type === 'BOOLEAN'"
      :model-value="modelValue === 'true'"
      @update:model-value="$emit('update:modelValue', $event ? 'true' : 'false')"
    />
    <!-- 下拉选择 -->
    <USelect
      v-else-if="field.type === 'SELECT'"
      :model-value="modelValue"
      :items="field.options"
      option-attribute="label"
      value-attribute="value"
      @update:model-value="$emit('update:modelValue', $event)"
    />
    <!-- 密码 -->
    <UInput
      v-else-if="field.type === 'PASSWORD'"
      :model-value="modelValue"
      type="password"
      :placeholder="field.placeholder"
      autocomplete="new-password"
      @update:model-value="$emit('update:modelValue', $event)"
    />
    <!-- TEXT / EMAIL / NUMBER -->
    <UInput
      v-else
      :model-value="modelValue"
      :type="field.type.toLowerCase()"
      :placeholder="field.placeholder"
      @update:model-value="$emit('update:modelValue', $event)"
    />
  </UFormField>
</template>
```

### 5.2 `ChannelConfigCard.vue` — 渠道配置卡片

```vue
<!-- components/notification-channels/ChannelConfigCard.vue -->
<script setup lang="ts">
const props = defineProps<{
  channel: ChannelConfigVO
  saving: boolean
}>()
const emit = defineEmits<{ save: [request: ChannelConfigSaveRequest] }>()

// 本地可编辑的副本（避免直接修改 props）
const localEnabled = ref(props.channel.enabled)
const localValues = reactive({ ...props.channel.values })

function handleSave() {
  // 过滤掉未修改的密码字段（值为 "***" 视为未修改）
  const valuesToSave = Object.fromEntries(
    Object.entries(localValues).filter(([key, val]) => {
      const fieldDef = props.channel.schema.find(f => f.key === key)
      return !(fieldDef?.type === 'PASSWORD' && val === '***')
    })
  )
  emit('save', { enabled: localEnabled.value, values: valuesToSave })
}
</script>

<template>
  <UCard>
    <template #header>
      <div class="flex items-center justify-between">
        <span class="font-semibold text-base">{{ channel.label }}</span>
        <div class="flex items-center gap-2">
          <span class="text-muted text-sm">{{ localEnabled ? '已启用' : '已禁用' }}</span>
          <USwitch v-model="localEnabled" />
        </div>
      </div>
    </template>

    <div class="space-y-4">
      <ChannelDynamicField
        v-for="field in channel.schema"
        :key="field.key"
        :field="field"
        v-model="localValues[field.key]"
      />
    </div>

    <template #footer>
      <div class="flex justify-end">
        <UButton :loading="saving" @click="handleSave">保存配置</UButton>
      </div>
    </template>
  </UCard>
</template>
```

### 5.3 `notification-channels.vue` — 页面

```vue
<!-- pages/systems/notification-channels.vue -->
<script setup lang="ts">
const { channels, loading, saving, errorMessage, loadChannels, saveChannel } =
  useNotificationChannels()

await loadChannels()

// 当前激活的 Tab（默认选第一个）
const activeChannel = ref(channels.value[0]?.channel ?? '')

const activeChannelData = computed(
  () => channels.value.find(c => c.channel === activeChannel.value) ?? null
)

// Toast 通知
const toast = useToast()

async function handleSave(request: ChannelConfigSaveRequest) {
  const ok = await saveChannel(activeChannel.value, request)
  if (ok) {
    toast.add({ title: '保存成功', color: 'success' })
  } else {
    toast.add({ title: '保存失败', description: errorMessage.value, color: 'error' })
  }
}
</script>

<template>
  <UDashboardPanel id="system-notification-channels">
    <template #header>
      <UDashboardNavbar title="通知渠道配置">
        <template #leading>
          <UDashboardSidebarCollapse />
        </template>
      </UDashboardNavbar>
    </template>

    <template #body>
      <div class="p-4">
        <!-- 渠道 Tab 列表 -->
        <UTabs
          v-model="activeChannel"
          :items="channels.map(c => ({ label: c.label, value: c.channel }))"
          class="mb-4"
        />

        <!-- 骨架屏 -->
        <div v-if="loading" class="space-y-3">
          <USkeleton v-for="n in 5" :key="n" class="h-10 w-full" />
        </div>

        <!-- 渠道配置卡片 -->
        <ChannelConfigCard
          v-else-if="activeChannelData"
          :channel="activeChannelData"
          :saving="saving"
          @save="handleSave"
        />
      </div>
    </template>
  </UDashboardPanel>
</template>
```

---

## 六、侧边栏菜单项

在侧边栏配置中（`app.config.ts` 或菜单数据）的「系统管理」分组下追加：

```typescript
{
  label: '通知渠道',
  icon: 'i-lucide-bell',
  to: '/systems/notification-channels'
}
```

---

## 七、关键交互细节

| 场景 | 处理方式 |
|------|----------|
| **密码字段** | 初始显示 `***`，用户清空后才视为要修改；保存时若值仍为 `***` 则不传该 key |
| **BOOLEAN 字段** | DB 存 `"true"/"false"` 字符串，前端切换时转换 |
| **SELECT 字段** | `options` 由 schema 提供，前端无需硬编码 |
| **未配置的渠道** | `values` 为空 Map，表单展示空白 + 默认值 placeholder |
| **保存成功** | Toast 提示成功，本地状态同步更新（密码值保持 `***`） |
| **保存失败** | Toast 展示错误信息，表单数据不丢失 |
| **新增渠道** | 只需后端 `NotificationChannelSchema` 枚举增加一项，前端零改动 |

---

## 八、实现顺序

1. **类型定义** — 在 `types/index.d.ts` 追加 `ChannelConfigVO` 等接口
2. **Composable** — 实现 `useNotificationChannels.ts`
3. **`ChannelDynamicField.vue`** — 最底层，无依赖，优先实现
4. **`ChannelConfigCard.vue`** — 依赖 `ChannelDynamicField`
5. **页面 `notification-channels.vue`** — 组装以上内容
6. **菜单配置** — 追加侧边栏入口
