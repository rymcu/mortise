import {
  fetchMemberClientSessions,
  revokeMemberClientSession
} from '@mortise/core-sdk'
import type { MemberClientSession } from '~/types/security'

export function useMemberSecurity() {
  const { $api } = useNuxtApp()
  const sessions = useState<MemberClientSession[]>('site-member-client-sessions', () => [])
  const loading = useState('site-member-security-loading', () => false)
  const error = useState('site-member-security-error', () => '')

  async function fetchSessions(): Promise<MemberClientSession[]> {
    loading.value = true
    error.value = ''
    try {
      sessions.value = await fetchMemberClientSessions($api)
      return sessions.value
    }
    catch (e) {
      error.value = e instanceof Error ? e.message : '获取设备会话失败'
      return []
    }
    finally {
      loading.value = false
    }
  }

  async function revokeSession(sessionId: string): Promise<boolean> {
    loading.value = true
    error.value = ''
    try {
      const ok = await revokeMemberClientSession($api, sessionId)
      if (ok) {
        sessions.value = sessions.value.map(session =>
          session.id === sessionId
            ? { ...session, status: 'revoked', revokedAt: new Date().toISOString() }
            : session
        )
      }
      return ok
    }
    catch (e) {
      error.value = e instanceof Error ? e.message : '撤销设备会话失败'
      return false
    }
    finally {
      loading.value = false
    }
  }

  return {
    sessions: readonly(sessions),
    loading: readonly(loading),
    error: readonly(error),
    fetchSessions,
    revokeSession
  }
}
