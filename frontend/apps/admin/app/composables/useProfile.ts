import type { ProfileInfo } from '~/types'

/**
 * 用户资料 composable
 * 使用 useState 实现跨页面状态共享
 */
export function useProfile() {
  const { $api } = useNuxtApp()
  const auth = useAuthStore()
  const { uploadFile } = useAppFileUpload()

  const profile = useState<ProfileInfo | null>('admin-profile', () => null)
  const loading = useState('admin-profile-loading', () => false)
  const error = useState('admin-profile-error', () => '')

  /** 获取用户资料 */
  async function fetchProfile(): Promise<ProfileInfo | null> {
    // 如果已有数据直接返回
    if (profile.value) {
      return profile.value
    }

    loading.value = true
    error.value = ''
    try {
      const res = await $api<{ code: number; data: ProfileInfo }>(
        '/api/v1/admin/auth/profile'
      )
      profile.value = res?.data ?? null
      return profile.value
    } catch (e) {
      error.value = e instanceof Error ? e.message : '获取资料失败'
      return null
    } finally {
      loading.value = false
    }
  }

  /** 上传头像，返回头像 URL */
  async function uploadAvatar(file: File): Promise<string | null> {
    loading.value = true
    error.value = ''
    try {
      return await uploadFile(file, {
        endpoint: '/api/v1/admin/files',
        fallbackMessage: '头像上传失败',
        accept: 'image/*',
        maxSize: 10 * 1024 * 1024,
        fileKindLabel: '头像图片',
      })
    } catch (e) {
      error.value = e instanceof Error ? e.message : '头像上传失败'
      return null
    } finally {
      loading.value = false
    }
  }

  /** 更新用户资料 */
  async function updateProfile(data: {
    nickname: string
    avatar?: string | null
  }): Promise<boolean> {
    loading.value = true
    error.value = ''
    try {
      await $api('/api/v1/admin/auth/profile', {
        method: 'PUT',
        body: data
      })
      // 同步刷新用户会话信息
      await auth.fetchCurrentUser()
      // 刷新本地资料数据（强制重新获取）
      profile.value = null
      await fetchProfile()
      return true
    } catch (e) {
      error.value = e instanceof Error ? e.message : '保存资料失败'
      return false
    } finally {
      loading.value = false
    }
  }

  /** 重读:发送邮箱更换验证码 */
  async function sendEmailUpdateCode(newEmail: string): Promise<boolean> {
    loading.value = true
    error.value = ''
    try {
      await $api('/api/v1/admin/auth/email/send-code', {
        method: 'POST',
        body: { newEmail }
      })
      return true
    } catch (e) {
      error.value = e instanceof Error ? e.message : '发送验证码失败'
      return false
    } finally {
      loading.value = false
    }
  }

  /** 确认邮箱更换 */
  async function confirmEmailUpdate(newEmail: string, code: string): Promise<boolean> {
    loading.value = true
    error.value = ''
    try {
      await $api('/api/v1/admin/auth/email/confirm', {
        method: 'PUT',
        body: { newEmail, code }
      })
      // 刷新会话和资料
      await auth.fetchCurrentUser()
      profile.value = null
      await fetchProfile()
      return true
    } catch (e) {
      error.value = e instanceof Error ? e.message : '邮箱更换失败'
      return false
    } finally {
      loading.value = false
    }
  }

  return {
    profile: readonly(profile),
    loading: readonly(loading),
    error: readonly(error),
    fetchProfile,
    uploadAvatar,
    updateProfile,
    sendEmailUpdateCode,
    confirmEmailUpdate
  }
}

