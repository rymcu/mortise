declare module 'isomorphic-dompurify' {
  type SanitizeOptions = Record<string, unknown>

  interface DOMPurifyLike {
    sanitize(dirty: string, options?: SanitizeOptions): string
  }

  const DOMPurify: DOMPurifyLike
  export default DOMPurify
}
