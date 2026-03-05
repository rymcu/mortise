import type { MemberProfile } from '~/types/profile'

/**
 * 会员资料 composable
 * 使用 useState 实现跨页面状态共享
 */
export function useProfile() {
  const { $api } = useNuxtApp()
  const auth = useAuthStore()

  const profile = useState<MemberProfile | null>('site-profile', () => null)
  const loading = useState('site-profile-loading', () => false)
  const error = useState('site-profile-error', () => '')

  /** 获取会员资料 */
  async function fetchProfile(): Promise<MemberProfile | null> {
    if (profile.value) {
      return profile.value
    }

    loading.value = true
    error.value = ''
    try {
      const res = await $api<{ code: number; message: string; data: MemberProfile }>(
        '/api/v1/app/auth/profile'
      )
      profile.value = res?.data ?? null
      return profile.value
    }
    catch (e) {
      error.value = e instanceof Error ? e.message : '获取资料失败'
      return null
    }
    finally {
      loading.value = false
    }
  }

  /** 上传头像，返回头像 URL */
  async function uploadAvatar(file: File): Promise<string | null> {
    loading.value = true
    error.value = ''
    try {
      const formData = new FormData()
      formData.append('file', file)
      const res = await $api<{ code: number; data: { url: string } }>(
        '/api/v1/app/files',
        { method: 'POST', body: formData }
      )
      return res?.data?.url ?? null
    }
    catch (e) {
      error.value = e instanceof Error ? e.message : '头像上传失败'
      return null
    }
    finally {
      loading.value = false
    }
  }

  /** 更新会员资料 */
  async function updateProfile(data: {
    nickname: string
    avatarUrl?: string | null
    gender?: string | null
    birthDate?: string | null
  }): Promise<boolean> {
    loading.value = true
    error.value = ''
    try {
      const res = await $api<{ code: number; message: string; data: boolean }>(
        '/api/v1/app/auth/profile',
        { method: 'PUT', body: data }
      )
      if (res.code !== 200) throw new Error(res.message || '保存失败')
      // 同步会话信息
      auth.setSessionUser({
        ...auth.session?.user,
        nickname: data.nickname,
        avatarUrl: data.avatarUrl || auth.session?.user?.avatarUrl
      })
      // 强制重新拉取最新资料
      profile.value = null
      await fetchProfile()
      return true
    }
    catch (e) {
      error.value = e instanceof Error ? e.message : '保存资料失败'
      return false
    }
    finally {
      loading.value = false
    }
  }

  /** 修改密码 */
  async function updatePassword(oldPassword: string, newPassword: string): Promise<boolean> {
    loading.value = true
    error.value = ''
    try {
      const res = await $api<{ code: number; message: string; data: boolean }>(
        '/api/v1/app/auth/password',
        { method: 'PUT', body: { oldPassword, newPassword } }
      )
      if (res.code !== 200) throw new Error(res.message || '修改失败')
      return true
    }
    catch (e) {
      error.value = e instanceof Error ? e.message : '修改密码失败'
      return false
    }
    finally {
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
    updatePassword
  }
}
