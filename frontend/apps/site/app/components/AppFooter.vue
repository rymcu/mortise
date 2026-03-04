<script setup lang="ts">
const columns = [{
  label: '资源',
  children: [{
    label: '文档',
    to: '/docs'
  }, {
    label: '快速开始',
    to: '/docs/getting-started'
  }, {
    label: '架构介绍',
    to: '/docs/architecture'
  }]
}, {
  label: '社区',
  children: [{
    label: '文章',
    to: '/articles'
  }, {
    label: '下载',
    to: '/download'
  }, {
    label: '关于我们',
    to: '/about'
  }]
}, {
  label: '项目',
  children: [{
    label: 'GitHub',
    to: 'https://github.com/mortise-framework',
    target: '_blank'
  }, {
    label: '开源协议',
    to: '/about#license'
  }, {
    label: '更新日志',
    to: '/docs/changelog'
  }]
}]
</script>

<template>
  <USeparator class="h-px" />

  <UFooter :ui="{ top: 'border-b border-default' }">
    <template #top>
      <UContainer>
        <UFooterColumns :columns="columns">
          <template #right>
            <div class="flex flex-col gap-2">
              <p class="text-sm font-medium">
                开源协议
              </p>
              <p class="text-sm text-muted">
                Mortise 采用 MIT 开源协议，欢迎贡献代码。
              </p>
            </div>
          </template>
        </UFooterColumns>
      </UContainer>
    </template>

    <template #left>
      <p class="text-sm text-muted">
        Mortise Framework • © {{ new Date().getFullYear() }} • MIT License
      </p>
    </template>

    <template #right>
      <UButton
        to="https://github.com/mortise-framework"
        target="_blank"
        icon="i-simple-icons-github"
        color="neutral"
        variant="ghost"
        aria-label="GitHub"
      />
    </template>
  </UFooter>
</template>
