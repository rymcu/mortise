export interface MemberProfile {
  id?: string
  username?: string
  nickname?: string
  avatarUrl?: string | null
  gender?: string | null
  birthDate?: string | null
  email?: string | null
  phone?: string | null
}

export type ProfileSection = 'info' | 'security'

export interface ProfileNavItem {
  key: ProfileSection
  label: string
  icon: string
  description: string
}

export interface ProfileUpdateForm {
  nickname: string
  avatarUrl: string
  gender: string
  birthDate: string
}

export interface PasswordUpdateForm {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}
