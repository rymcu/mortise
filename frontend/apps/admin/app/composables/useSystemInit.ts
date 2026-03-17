/**
 * 系统初始化相关 API 组合式函数
 */

import type { GlobalResult } from '@mortise/core-sdk'
import type { SystemInitInfo } from '~/types/system-init'

export function useSystemInit() {
  const config = useRuntimeConfig()
  const baseURL = config.public.apiBase

  /**
   * 确保 apiBase 已配置，避免请求发到 Nuxt 自身的开发服务器
   */
  function ensureApiBase() {
    if (!baseURL) {
      throw new Error('runtimeConfig.public.apiBase 未配置，无法请求后端 API')
    }
  }

  /**
   * 检查系统是否已初始化
   */
  async function checkInitStatus(): Promise<boolean> {
    ensureApiBase()
    const result = await $fetch<GlobalResult<{ initialized: boolean }>>(
      '/api/v1/admin/system-init/status',
      { baseURL }
    )
    return result.data?.initialized ?? false
  }

  /**
   * 执行系统初始化
   */
  async function initializeSystem(initInfo: SystemInitInfo): Promise<string> {
    ensureApiBase()
    const result = await $fetch<GlobalResult<string>>(
      '/api/v1/admin/system-init/initialize',
      {
        method: 'POST',
        baseURL,
        body: initInfo
      }
    )
    if (result.code !== 200) {
      throw new Error(result.message || '系统初始化失败')
    }
    return result.data
  }

  /**
   * 获取初始化进度（0-100）
   */
  async function getInitProgress(): Promise<number> {
    ensureApiBase()
    const result = await $fetch<GlobalResult<{ progress: number }>>(
      '/api/v1/admin/system-init/progress',
      { baseURL }
    )
    return result.data?.progress ?? 0
  }

  return {
    checkInitStatus,
    initializeSystem,
    getInitProgress
  }
}
