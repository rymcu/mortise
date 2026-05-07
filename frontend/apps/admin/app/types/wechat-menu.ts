export type WeChatMenuButton = {
  type?: string
  name?: string
  key?: string
  url?: string
  mediaId?: string
  articleId?: string
  appId?: string
  pagePath?: string
  subButtons?: WeChatMenuButton[]
  sub_button?: WeChatMenuButton[]
}

export type WeChatMenu = {
  buttons?: WeChatMenuButton[]
  button?: WeChatMenuButton[]
}

export type WeChatMpConditionalMenu = {
  buttons?: WeChatMenuButton[]
  button?: WeChatMenuButton[]
  menuId?: string
  rule?: Record<string, string>
}

export type WeChatMpMenu = {
  menu?: WeChatMpConditionalMenu
  conditionalMenu?: WeChatMpConditionalMenu[]
}

export type WeChatMenuAccount = {
  id?: string
  accountName?: string
  accountType?: string
  appId?: string
}

export type WeChatMenuTypeOption = {
  label: string
  value: string
}

export type WeChatMenuFieldVisibility = {
  key: boolean
  url: boolean
  mediaId: boolean
  articleId: boolean
  miniProgram: boolean
}
