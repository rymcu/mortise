declare module 'isomorphic-dompurify' {
  interface SanitizeOptions extends Record<string, unknown> {}

  interface DOMPurifyLike {
    sanitize(dirty: string, options?: SanitizeOptions): string
  }

  const DOMPurify: DOMPurifyLike
  export default DOMPurify
}
