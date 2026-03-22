import type { RouterConfig } from 'nuxt/schema'

/**
 * 自定义路由滚动行为。
 *
 * 默认 Nuxt 行为在导航到 hash 锚点时会立即尝试查找 DOM 元素，
 * 但评论等异步加载的内容此时可能尚未渲染，导致 Vue Router 警告：
 *   "[Vue Router warn]: Couldn't find element using selector ..."
 *
 * 本配置在 hash 目标元素不存在时跳过滚动，交由页面级代码
 * （如文章详情页的 ensureCommentAnchorVisible）处理异步滚动。
 */
export default <RouterConfig>{
  scrollBehavior(to, _from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }
    if (to.hash) {
      const el = document.querySelector(to.hash)
      if (el) {
        return { el: to.hash, behavior: 'smooth' }
      }
      // 元素尚未渲染，不滚动；由页面级 watcher 接管
      return false
    }
    return { top: 0 }
  },
}
