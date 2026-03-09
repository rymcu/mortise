<script setup lang="ts">
const props = defineProps<{
  src?: string
  alt?: string
  title?: string
  width?: string | number
  height?: string | number
}>()

const attrs = useAttrs()
const { resolveUrl } = useMediaUrl()
const rawAttrs = computed(() => attrs as Record<string, unknown>)
const forwardedAttrs = computed(() => {
  const { loading: _loading, decoding: _decoding, src: _src, alt: _alt, width: _width, height: _height, ...rest } = rawAttrs.value
  return rest
})

const resolvedSrc = computed(() => resolveUrl(props.src) ?? props.src ?? '')
const resolvedAlt = computed(() => props.alt ?? '')
const resolvedWidth = computed(() => props.width ?? undefined)
const resolvedHeight = computed(() => props.height ?? undefined)
const caption = computed(() => props.title?.trim() || '')
const loading = computed<'lazy' | 'eager'>(( ) => rawAttrs.value.loading === 'eager' ? 'eager' : 'lazy')
const decoding = computed<'async' | 'auto' | 'sync'>(() => {
  const value = rawAttrs.value.decoding
  if (value === 'auto' || value === 'sync') {
    return value
  }
  return 'async'
})
const imageAttrs = computed<Record<string, unknown>>(() => ({
  ...forwardedAttrs.value,
  src: resolvedSrc.value,
  alt: resolvedAlt.value,
  width: resolvedWidth.value,
  height: resolvedHeight.value
}))
</script>

<template>
  <figure class="my-8 overflow-hidden rounded-2xl border border-default bg-muted/20">
    <img
      v-bind="imageAttrs"
      :loading="loading"
      :decoding="decoding"
      class="block h-auto w-full max-w-full object-contain"
    >
    <figcaption v-if="caption" class="border-t border-default/80 px-4 py-3 text-center text-sm text-muted">
      {{ caption }}
    </figcaption>
  </figure>
</template>