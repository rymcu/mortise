<script setup lang="ts">
import {
  fetchAdminDelete,
  fetchAdminGet,
  fetchAdminPut
} from '@mortise/core-sdk'
import type {
  WeChatMenu,
  WeChatMenuAccount,
  WeChatMenuButton,
  WeChatMenuFieldVisibility,
  WeChatMenuTypeOption,
  WeChatMpMenu
} from '~/types/wechat-menu'

const open = defineModel<boolean>('open', { default: false })

const props = defineProps<{
  account: WeChatMenuAccount
}>()

const emit = defineEmits<{
  (e: 'success'): void
}>()

const { $api } = useNuxtApp()

const loading = ref(false)
const errorMessage = ref('')
const menuButtons = ref<WeChatMenuButton[]>([])
const selectedPath = ref<number[]>([])

const rootLimit = 3
const subLimit = 5
const keyTypes = [
  'click',
  'scancode_push',
  'scancode_waitmsg',
  'pic_sysphoto',
  'pic_photo_or_album',
  'pic_weixin',
  'location_select'
]

const typeOptions: WeChatMenuTypeOption[] = [
  { value: 'click', label: '点击事件' },
  { value: 'view', label: '网页链接' },
  { value: 'miniprogram', label: '小程序' },
  { value: 'scancode_push', label: '扫码推事件' },
  { value: 'scancode_waitmsg', label: '扫码带提示' },
  { value: 'pic_sysphoto', label: '系统拍照' },
  { value: 'pic_photo_or_album', label: '拍照或相册' },
  { value: 'pic_weixin', label: '微信相册' },
  { value: 'location_select', label: '地理位置' },
  { value: 'media_id', label: '永久素材' },
  { value: 'view_limited', label: '图文素材' },
  { value: 'article_id', label: '发布图文' },
  { value: 'article_view_limited', label: '发布图文链接' }
]

const accountId = computed(() => String(props.account.id || ''))
const isMpAccount = computed(() => {
  const accountType = String(props.account.accountType || '').toLowerCase()
  return accountType === 'mp' || accountType === '公众号'
})
const menuPath = computed(() => `/api/v1/admin/wechat/accounts/${accountId.value}/menu`)

const subButtonsOf = (button: WeChatMenuButton) => button.subButtons ?? button.sub_button ?? []

const copyButtons = (buttons: WeChatMenuButton[] = []): WeChatMenuButton[] =>
  buttons.map(button => ({
    name: button.name ?? '',
    type: button.type ?? 'click',
    key: button.key ?? '',
    url: button.url ?? '',
    mediaId: button.mediaId ?? '',
    articleId: button.articleId ?? '',
    appId: button.appId ?? '',
    pagePath: button.pagePath ?? '',
    subButtons: copyButtons(subButtonsOf(button))
  }))

const createButton = (name: string): WeChatMenuButton => ({
  name,
  type: 'click',
  key: '',
  url: '',
  mediaId: '',
  articleId: '',
  appId: '',
  pagePath: '',
  subButtons: []
})

const getButton = (path: number[]) => {
  if (!path.length) return null
  const rootIndex = path[0]
  if (rootIndex === undefined) return null
  const root = menuButtons.value[rootIndex]
  if (!root) return null
  const subIndex = path[1]
  return path.length === 1 || subIndex === undefined ? root : subButtonsOf(root)[subIndex] ?? null
}

const activeButton = computed(() => getButton(selectedPath.value))
const activeRootButton = computed(() => getButton(selectedPath.value.slice(0, 1)))
const activeHasChildren = computed(() => Boolean(activeButton.value && subButtonsOf(activeButton.value).length))
const canAddRoot = computed(() => menuButtons.value.length < rootLimit)
const canAddSub = computed(() => Boolean(activeRootButton.value) && subButtonsOf(activeRootButton.value!).length < subLimit)

const visibleFields = computed<WeChatMenuFieldVisibility>(() => {
  const type = activeButton.value?.type || ''
  return {
    key: keyTypes.includes(type),
    url: ['view', 'miniprogram'].includes(type),
    mediaId: ['media_id', 'view_limited'].includes(type),
    articleId: ['article_id', 'article_view_limited'].includes(type),
    miniProgram: type === 'miniprogram'
  }
})

function isSelected(path: number[]) {
  return selectedPath.value.length === path.length && path.every((item, index) => selectedPath.value[index] === item)
}

async function loadMenu() {
  if (!accountId.value) return
  loading.value = true
  errorMessage.value = ''

  try {
    const data = await fetchAdminGet<WeChatMpMenu>($api, menuPath.value)
    const buttons = data?.menu?.buttons ?? data?.menu?.button ?? []
    menuButtons.value = copyButtons(buttons)
    selectedPath.value = menuButtons.value.length ? [0] : []
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '读取菜单失败'
  } finally {
    loading.value = false
  }
}

watch(open, value => {
  if (!value) return
  if (!isMpAccount.value) {
    errorMessage.value = '仅公众号账号支持自定义菜单维护'
    menuButtons.value = []
    selectedPath.value = []
    return
  }
  loadMenu()
})

function addRootButton() {
  if (!canAddRoot.value) return
  menuButtons.value.push(createButton(`菜单 ${menuButtons.value.length + 1}`))
  selectedPath.value = [menuButtons.value.length - 1]
}

function addSubButton() {
  const root = activeRootButton.value
  const rootIndex = selectedPath.value[0]
  if (!root || !canAddSub.value || rootIndex === undefined) return
  root.subButtons = subButtonsOf(root)
  root.subButtons.push(createButton(`子菜单 ${root.subButtons.length + 1}`))
  selectedPath.value = [rootIndex, root.subButtons.length - 1]
}

function removeSelectedButton() {
  if (!selectedPath.value.length) return
  const rootIndex = selectedPath.value[0]
  if (rootIndex === undefined) return
  if (selectedPath.value.length === 1) {
    menuButtons.value.splice(rootIndex, 1)
    selectedPath.value = menuButtons.value.length ? [Math.max(0, rootIndex - 1)] : []
    return
  }

  const subIndex = selectedPath.value[1]
  if (subIndex === undefined) return
  const root = menuButtons.value[rootIndex]
  root?.subButtons?.splice(subIndex, 1)
  selectedPath.value = [rootIndex]
}

const trimText = (value?: string) => value?.trim() || undefined

function sanitizeButton(button: WeChatMenuButton): WeChatMenuButton {
  const children = subButtonsOf(button).map(sanitizeButton).filter(item => item.name)
  if (children.length) {
    return {
      name: trimText(button.name),
      subButtons: children
    }
  }

  const payload: WeChatMenuButton = {
    name: trimText(button.name),
    type: button.type || 'click'
  }

  if (keyTypes.includes(payload.type || '')) payload.key = trimText(button.key)
  if (['view', 'miniprogram'].includes(payload.type || '')) payload.url = trimText(button.url)
  if (['media_id', 'view_limited'].includes(payload.type || '')) payload.mediaId = trimText(button.mediaId)
  if (['article_id', 'article_view_limited'].includes(payload.type || '')) payload.articleId = trimText(button.articleId)
  if (payload.type === 'miniprogram') {
    payload.appId = trimText(button.appId)
    payload.pagePath = trimText(button.pagePath)
  }

  return payload
}

function validateButton(button: WeChatMenuButton): string | null {
  if (!trimText(button.name)) return '菜单名称不能为空'
  const children = subButtonsOf(button)
  if (children.length) {
    for (const child of children) {
      const message = validateButton(child)
      if (message) return message
    }
    return null
  }

  if (!button.type) return '请选择菜单类型'
  if (keyTypes.includes(button.type) && !trimText(button.key)) return '当前菜单类型需要填写菜单 Key'
  if (['view', 'miniprogram'].includes(button.type) && !trimText(button.url)) return '当前菜单类型需要填写跳转链接'
  if (button.type === 'miniprogram' && !trimText(button.appId)) return '当前菜单类型需要填写小程序 AppID'
  if (button.type === 'miniprogram' && !trimText(button.pagePath)) return '当前菜单类型需要填写小程序路径'
  if (['media_id', 'view_limited'].includes(button.type) && !trimText(button.mediaId)) return '当前菜单类型需要填写素材 Media ID'
  if (['article_id', 'article_view_limited'].includes(button.type) && !trimText(button.articleId)) return '当前菜单类型需要填写图文 Article ID'
  return null
}

function validateMenu() {
  if (!menuButtons.value.length) return '请至少添加一个一级菜单'
  for (const button of menuButtons.value) {
    const message = validateButton(button)
    if (message) return message
  }
  return null
}

async function saveMenu() {
  const validationMessage = validateMenu()
  if (validationMessage) {
    errorMessage.value = validationMessage
    return
  }

  loading.value = true
  errorMessage.value = ''
  try {
    const payload: WeChatMenu = { buttons: menuButtons.value.map(sanitizeButton) }
    await fetchAdminPut<string>($api, menuPath.value, payload)
    emit('success')
    await loadMenu()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '发布菜单失败'
  } finally {
    loading.value = false
  }
}

async function deleteRemoteMenu() {
  if (!window.confirm('确定删除该账号已发布的微信菜单吗？')) return
  loading.value = true
  errorMessage.value = ''
  try {
    await fetchAdminDelete<boolean>($api, menuPath.value)
    menuButtons.value = []
    selectedPath.value = []
    emit('success')
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '删除菜单失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <UModal v-model:open="open" title="微信菜单维护" :ui="{ content: 'sm:max-w-6xl' }">
    <template #body>
      <div class="space-y-4">
        <UAlert
          v-if="errorMessage"
          color="error"
          :title="errorMessage"
        />

        <div class="flex flex-wrap items-center justify-between gap-3">
          <div class="min-w-0">
            <div class="truncate text-sm font-medium">
              {{ account.accountName || account.appId || '微信账号' }}
            </div>
            <div class="truncate text-xs text-muted">
              {{ account.appId }}
            </div>
          </div>
          <div class="flex flex-wrap gap-2">
            <UButton icon="i-lucide-refresh-cw" color="neutral" variant="subtle" :loading="loading" @click="loadMenu">
              读取菜单
            </UButton>
            <UButton icon="i-lucide-plus" color="primary" variant="subtle" :disabled="!canAddRoot" @click="addRootButton">
              一级菜单
            </UButton>
          </div>
        </div>

        <div class="grid grid-cols-1 gap-4 lg:grid-cols-[280px_1fr]">
          <div class="rounded-md border border-default p-3">
            <div class="mb-3 flex items-center justify-between gap-2">
              <div class="text-sm font-medium">一级菜单</div>
              <UBadge variant="subtle">{{ menuButtons.length }} / {{ rootLimit }}</UBadge>
            </div>

            <div v-if="!menuButtons.length" class="rounded-md border border-dashed border-default p-6 text-center text-sm text-muted">
              暂无菜单
            </div>

            <div v-else class="space-y-2">
              <div v-for="(button, rootIndex) in menuButtons" :key="`root-${rootIndex}`" class="space-y-1">
                <button
                  type="button"
                  class="flex w-full items-center justify-between rounded-md px-3 py-2 text-left text-sm transition-colors"
                  :class="isSelected([rootIndex]) ? 'bg-primary/10 text-primary' : 'hover:bg-muted'"
                  @click="selectedPath = [rootIndex]"
                >
                  <span class="truncate">{{ button.name || '未命名' }}</span>
                  <UBadge size="sm" variant="subtle">{{ subButtonsOf(button).length }}</UBadge>
                </button>

                <div v-if="subButtonsOf(button).length" class="ml-4 space-y-1 border-l border-default pl-2">
                  <button
                    v-for="(subButton, subIndex) in subButtonsOf(button)"
                    :key="`sub-${rootIndex}-${subIndex}`"
                    type="button"
                    class="flex w-full items-center rounded-md px-3 py-2 text-left text-sm transition-colors"
                    :class="isSelected([rootIndex, subIndex]) ? 'bg-primary/10 text-primary' : 'hover:bg-muted'"
                    @click="selectedPath = [rootIndex, subIndex]"
                  >
                    <span class="truncate">{{ subButton.name || '未命名' }}</span>
                  </button>
                </div>
              </div>
            </div>
          </div>

          <div class="rounded-md border border-default p-4">
            <div v-if="!activeButton" class="flex min-h-56 items-center justify-center text-sm text-muted">
              请选择菜单
            </div>

            <div v-else class="space-y-4">
              <div class="flex items-center justify-between gap-2">
                <div class="text-sm font-medium">菜单内容</div>
                <div class="flex gap-2">
                  <UButton icon="i-lucide-list-plus" color="neutral" variant="subtle" :disabled="!canAddSub" @click="addSubButton">
                    子菜单
                  </UButton>
                  <UButton icon="i-lucide-trash-2" color="error" variant="subtle" @click="removeSelectedButton">
                    移除
                  </UButton>
                </div>
              </div>

              <div class="grid grid-cols-1 gap-4 md:grid-cols-2">
                <UFormField label="菜单名称" class="w-full">
                  <UInput v-model="activeButton.name" class="w-full" placeholder="请输入菜单名称" />
                </UFormField>

                <UFormField v-if="!activeHasChildren" label="菜单类型" class="w-full">
                  <USelect v-model="activeButton.type" class="w-full" :items="typeOptions" />
                </UFormField>

                <UFormField v-if="!activeHasChildren && visibleFields.key" label="菜单 Key" class="w-full">
                  <UInput v-model="activeButton.key" class="w-full" placeholder="请输入事件 Key" />
                </UFormField>

                <UFormField v-if="!activeHasChildren && visibleFields.url" label="跳转链接" class="w-full">
                  <UInput v-model="activeButton.url" class="w-full" placeholder="请输入 HTTPS 链接" />
                </UFormField>

                <UFormField v-if="!activeHasChildren && visibleFields.mediaId" label="素材 Media ID" class="w-full">
                  <UInput v-model="activeButton.mediaId" class="w-full" placeholder="请输入素材 Media ID" />
                </UFormField>

                <UFormField v-if="!activeHasChildren && visibleFields.articleId" label="图文 Article ID" class="w-full">
                  <UInput v-model="activeButton.articleId" class="w-full" placeholder="请输入图文 Article ID" />
                </UFormField>

                <UFormField v-if="!activeHasChildren && visibleFields.miniProgram" label="小程序 AppID" class="w-full">
                  <UInput v-model="activeButton.appId" class="w-full" placeholder="请输入小程序 AppID" />
                </UFormField>

                <UFormField v-if="!activeHasChildren && visibleFields.miniProgram" label="小程序路径" class="w-full">
                  <UInput v-model="activeButton.pagePath" class="w-full" placeholder="请输入小程序页面路径" />
                </UFormField>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>

    <template #footer>
      <div class="flex w-full justify-between gap-2">
        <UButton icon="i-lucide-cloud-off" color="error" variant="subtle" :loading="loading" @click="deleteRemoteMenu">
          删除线上菜单
        </UButton>
        <div class="flex gap-2">
          <UButton variant="ghost" label="取消" @click="open = false" />
          <UButton label="发布菜单" :loading="loading" @click="saveMenu" />
        </div>
      </div>
    </template>
  </UModal>
</template>
