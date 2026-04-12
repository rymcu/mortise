export interface FooterColumnLinkEditorValue {
  id: string
  label: string
  to: string
  target: string
}

export interface FooterColumnEditorValue {
  id: string
  label: string
  children: FooterColumnLinkEditorValue[]
}

export interface ParsedFooterColumnsEditorValue {
  columns: FooterColumnEditorValue[]
  hasInvalidInput: boolean
}
