# ========== 构建阶段 ==========
FROM node:22-alpine AS builder

WORKDIR /app

ARG APP_NAME

ENV COREPACK_ENABLE_DOWNLOAD_PROMPT=0

RUN corepack enable

# 先复制依赖清单，提升缓存命中率
COPY package.json ./
COPY pnpm-lock.yaml ./
COPY pnpm-workspace.yaml ./
COPY apps/admin/package.json ./apps/admin/package.json
COPY apps/web/package.json ./apps/web/package.json
COPY apps/site/package.json ./apps/site/package.json
COPY packages/auth/package.json ./packages/auth/package.json
COPY packages/config/package.json ./packages/config/package.json
COPY packages/core-sdk/package.json ./packages/core-sdk/package.json
COPY packages/ui/package.json ./packages/ui/package.json

RUN pnpm install --frozen-lockfile

# 再复制源码并构建指定应用
COPY . .

# 构建时给 Node 预留更充足内存
ENV NODE_OPTIONS=--max-old-space-size=2048

RUN pnpm --filter @mortise/${APP_NAME} build

# ========== 运行阶段 ==========
FROM node:22-alpine

WORKDIR /app

ARG APP_NAME

ENV NODE_ENV=production
ENV NODE_OPTIONS=--max-old-space-size=512
ENV HOST=0.0.0.0
ENV PORT=3000

# 仅复制运行所需产物
COPY --from=builder /app/apps/${APP_NAME}/.output /app/.output

RUN addgroup -g 1001 -S nodejs && \
    adduser -S nuxt -u 1001 && \
    chown -R nuxt:nodejs /app

USER nuxt

EXPOSE 3000

CMD ["node", ".output/server/index.mjs"]
