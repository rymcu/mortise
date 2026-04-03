export type UiColor = 'primary' | 'secondary' | 'success' | 'info' | 'warning' | 'error' | 'neutral'
export type ProfileSection = 'info' | 'security'

export interface ProfileNavItem {
  key: ProfileSection
  label: string
  icon: string
  description: string
}

export interface MemberProfile {
  username: string
  nickname?: string | null
  avatarUrl?: string | null
  email?: string | null
  phone?: string | null
  gender?: string | null
  birthDate?: string | null
}

export interface PasswordStrengthRule {
  met: boolean
  text: string
}

export interface PasswordFormProps {
  currentPassword: string
  newPassword: string
  confirmPassword: string
  showCurrent: boolean
  showNew: boolean
  showConfirm: boolean
  loading: boolean
  score: number
  strength: PasswordStrengthRule[]
  color: UiColor
  strengthText: string
  confirmMatched: boolean
  canChangePassword: boolean
  error: string
  success: string
}
