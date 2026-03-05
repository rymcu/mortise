export interface MemberProfile {
  id?: number
  username?: string
  nickname?: string
  avatarUrl?: string | null
  gender?: string | null
  birthDate?: string | null
  email?: string | null
  phone?: string | null
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
