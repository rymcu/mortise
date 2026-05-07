export type RichTextEditorContentType = 'html' | 'markdown' | 'json'

export type RichTextEditorToolbarItem = Record<string, unknown>

export type RichTextEditorToolbarGroup = RichTextEditorToolbarItem[]

export type RichTextEditorMentionItem = Record<string, unknown>

import type { CompatMediaPickerProvider } from './media-picker'

export interface RichTextEditorCommandChain {
  focus: () => RichTextEditorCommandChain
  setImage: (attributes: Record<string, unknown>) => RichTextEditorCommandChain
  insertContent: (content: Record<string, unknown>) => RichTextEditorCommandChain
  run: () => void
}

export interface RichTextEditorInstance {
  chain: () => RichTextEditorCommandChain
}

export interface CompatRichTextEditorProps {
  placeholder?: string
  minHeightClass?: string
  contentType?: RichTextEditorContentType
  toolbarItems?: RichTextEditorToolbarGroup[]
  toolbarButtonSize?: 'xs' | 'sm' | 'md'
  enableImageUpload?: boolean
  uploadEndpoint?: string
  uploadAccept?: string
  uploadMaxSize?: number
  uploadFileKindLabel?: string
  resolveUploadedUrl?: boolean
  enableAssetPicker?: boolean
  assetPickerProvider?: CompatMediaPickerProvider | null
  assetPickerFilterValues?: Record<string, string>
  assetPickerTitle?: string
  assetPickerDescription?: string
  showMentionMenu?: boolean
  mentionMenuItems?: RichTextEditorMentionItem[]
  mentionMenuIgnoreFilter?: boolean
}
