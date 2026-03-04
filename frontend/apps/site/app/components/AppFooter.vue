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
  label: '产品',
  children: [{
    label: '模块总览',
    to: '/modules'
  }, {
    label: '定价方案',
    to: '/pricing'
  }, {
    label: '下载',
    to: '/download'
  }]
}, {
  label: '社区',
  children: [{
    label: '文章',
    to: '/blog'
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
    to: '/changelog'
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
                主仓库基础模块 MIT 协议永久免费开源，商业扩展模块单独定价授权。
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
