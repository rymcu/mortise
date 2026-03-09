<script setup lang="ts">
const props = defineProps<{
  href?: string
  title?: string
  target?: string
  rel?: string
}>()

const href = computed(() => props.href ?? '')
const isExternal = computed(() => /^(https?:)?\/\//.test(href.value) || /^(mailto:|tel:)/.test(href.value))
const resolvedTarget = computed(() => props.target ?? (isExternal.value ? '_blank' : undefined))
const resolvedRel = computed(() => {
  const relTokens = new Set((props.rel ?? '').split(/\s+/).filter(Boolean))

  if (isExternal.value && resolvedTarget.value === '_blank') {
    relTokens.add('noopener')
    relTokens.add('noreferrer')
  }

  return relTokens.size > 0 ? Array.from(relTokens).join(' ') : undefined
})
</script>

<template>
  <a
    :href="href"
    :title="title"
    :target="resolvedTarget"
    :rel="resolvedRel"
    class="wrap-break-word underline decoration-primary/35 underline-offset-4 transition-colors hover:text-primary hover:decoration-primary"
  >
    <slot />
  </a>
</template>