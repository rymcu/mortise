import { resolve } from 'node:path'
import { resolveAppLayerExtends } from '../../scripts/layer-discovery.mjs'

const appRoot = __dirname
const layersRoot = resolve(__dirname, '../../layers')
const layers = resolveAppLayerExtends({
    appRoot,
    layersRoot,
    localLayersBase: '../../layers',
    appKind: 'site'
})

export default defineNuxtConfig({
    extends: layers,

    modules: ['@nuxt/eslint', '@nuxt/ui', '@pinia/nuxt', '@nuxt/content', '@nuxt/image'],

    ssr: true,

    devtools: {enabled: true},

    css: ['~/assets/css/main.css'],

    mdc: {
        highlight: {
            noApiRoute: false,
            langs: ['diff', 'ts', 'vue', 'css', 'java', 'xml', 'yaml', 'json', 'bash', 'sql']
        }
    },

    ui: {
        fonts: false
    },

    runtimeConfig: {
        public: {
            // 优先用环境变量，默认本地开发地址
            apiBase: process.env.NUXT_PUBLIC_API_BASE || 'http://localhost:9999/mortise',
            auth: {
                loginPath: '/api/v1/app/auth/login',
                refreshPath: '/api/v1/app/auth/refresh-token',
                callbackPath: '/api/v1/app/oauth2/callback',
                mePath: '/api/v1/app/auth/profile',
                oauthAuthUrlPath: '/api/v1/app/oauth2/auth-url'
            }
        }
    },

    devServer: {
        port: 3001
    },

    compatibilityDate: '2025-01-15',

    vite: {
        ssr: {
            external: ['isomorphic-dompurify']
        },
        optimizeDeps: {
            exclude: ['@nuxtjs/mdc'],
            include: ['extend']
        },
        server: {
            proxy: {
                '/mortise': {
                    target: 'http://localhost:9999',
                    changeOrigin: true
                }
            }
        }
    },

    eslint: {
        config: {
            stylistic: {
                commaDangle: 'never',
                braceStyle: '1tbs'
            }
        }
    },
})
