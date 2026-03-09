import ProseA from '../components/content/ProseA.vue'
import ProseImg from '../components/content/ProseImg.vue'

export default defineNuxtPlugin((nuxtApp) => {
  nuxtApp.vueApp.component('ProseA', ProseA)
  nuxtApp.vueApp.component('ProseImg', ProseImg)
})