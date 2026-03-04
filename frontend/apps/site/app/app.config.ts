export default defineAppConfig({
  // GitHub 仓库地址，用于获取更新日志
  repository: 'rymcu/mortise',
  ui: {
    colors: {
      primary: 'green',
      neutral: 'zinc'
    },
    prose: {
      li: {
        base: 'break-words'
      },
      a: {
        base: 'break-words'
      }
    }
  }
})
