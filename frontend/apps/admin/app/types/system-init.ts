/** 系统初始化信息 */
export interface SystemInitInfo {
  /** 管理员密码 */
  adminPassword: string
  /** 管理员昵称 */
  adminNickname: string
  /** 管理员邮箱 */
  adminEmail: string
  /** 系统名称 */
  systemName: string
  /** 系统描述 */
  systemDescription?: string
}