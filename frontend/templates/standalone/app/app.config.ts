/**
 * 独立部署应用配置
 *
 * ★ 覆盖业务 Layer 的 basePath，使链接指向根路径。
 * 每个 Layer 约定在 appConfig 中使用自己的命名空间，例如：
 *   community → appConfig.community.basePath
 *   commerce  → appConfig.commerce.basePath
 *
 * 复制本模板后，按所接入的 Layer 添加对应的 basePath 覆盖。
 */
export default defineAppConfig({
  // ★ 示例：接入 community-layer 时取消注释
  // community: {
  //   basePath: '',
  // },
  ui: {
    colors: {
      primary: 'green',
      neutral: 'zinc',
    },
  },
})
