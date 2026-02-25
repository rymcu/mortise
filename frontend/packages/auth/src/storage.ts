import type { AuthSession } from './types'

const SESSION_KEY = 'mortise.auth.session'

export function loadSession(): AuthSession | null {
  if (typeof localStorage === 'undefined') {
    return null
  }

  const raw = localStorage.getItem(SESSION_KEY)
  if (!raw) {
    return null
  }

  try {
    return JSON.parse(raw) as AuthSession
  } catch {
    localStorage.removeItem(SESSION_KEY)
    return null
  }
}

export function saveSession(session: AuthSession): void {
  if (typeof localStorage === 'undefined') {
    return
  }
  localStorage.setItem(SESSION_KEY, JSON.stringify(session))
}

export function clearSession(): void {
  if (typeof localStorage === 'undefined') {
    return
  }
  localStorage.removeItem(SESSION_KEY)
}
