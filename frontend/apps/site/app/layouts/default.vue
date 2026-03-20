<template>
  <div class="flex min-h-screen flex-col">
    <AppHeader />
    <main class="flex-1">
      <slot />
    </main>
    <AppFooter />
    <AgentChatFab />
    <SiteChatWidget />
  </div>
</template>
