/**
 * 系统初始化状态检测插件（客户端）
 *
 * 在应用启动时检查系统是否已初始化，
 * 未初始化则重定向到初始化引导页面。
 */
export default defineNuxtPlugin(async () => {
  const config = useRuntimeConfig()
  const initialized = useState<boolean | null>('system-initialized', () => null)

  // apiBase 未配置时跳过检测，避免请求打到 Nuxt 自身的开发服务器
  if (!config.public.apiBase) {
    console.warn('[system-init] runtimeConfig.public.apiBase 未配置，跳过系统初始化状态检测')
    initialized.value = true
    return
  }

  const { checkInitStatus } = useSystemInit()

  try {
    initialized.value = await checkInitStatus()
  } catch {
    // 接口不可用时默认视为已初始化，避免阻塞正常流程
    initialized.value = true
  }
})
