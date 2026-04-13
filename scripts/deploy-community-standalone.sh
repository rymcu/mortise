#!/usr/bin/env bash

set -euo pipefail

DEPLOY_HOST="192.168.88.146"
SITE_MODE="standalone-root"
SKIP_BACKEND=false
SKIP_SITE=false
SKIP_SMOKE=false
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
STANDALONE_APP_NAME="community-standalone-deploy"
BETTER_SQLITE3_VERSION="12.6.2"

write_step() {
  printf '\n==> %s\n' "$1" >&2
}

fail() {
  printf 'ERROR: %s\n' "$1" >&2
  exit 1
}

assert_command() {
  command -v "$1" >/dev/null 2>&1 || fail "未找到命令: $1"
}

invoke_remote_bash() {
  local remote_host="$1"
  shift
  local script
  script="$(printf '%s\n' "$@")"
  local encoded
  encoded="$(printf '%s' "$script" | base64 | tr -d '\n')"
  ssh "root@${remote_host}" "printf '%s' '${encoded}' | base64 -d | bash"
}

cleanup() {
  if [[ -n "${TEMP_APP_PATH:-}" && -d "${TEMP_APP_PATH}" ]]; then
    rm -rf "${TEMP_APP_PATH}"
  fi

  if [[ -n "${LOCK_BACKUP_PATH:-}" && -f "${LOCK_BACKUP_PATH}" ]]; then
    cp "${LOCK_BACKUP_PATH}" "${LOCK_PATH}"
    rm -f "${LOCK_BACKUP_PATH}"
  fi
}

usage() {
  cat <<'EOF'
用法:
  ./scripts/deploy-community-standalone.sh [options]

选项:
  --host <host>                  目标主机，默认 192.168.88.146
  --site-mode <mode>             standalone-root 或 site-community
  --skip-backend                 不发布后端
  --skip-site                    不发布站点
  --skip-smoke                   跳过 smoke 校验
  --project-root <path>          项目根目录
  --standalone-app-name <name>   临时 standalone 工位名，默认 community-standalone-deploy
  --better-sqlite3-version <v>   远端修复 better-sqlite3 的版本
  -h, --help                     显示帮助
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --host)
      DEPLOY_HOST="$2"
      shift 2
      ;;
    --site-mode)
      SITE_MODE="$2"
      shift 2
      ;;
    --skip-backend)
      SKIP_BACKEND=true
      shift
      ;;
    --skip-site)
      SKIP_SITE=true
      shift
      ;;
    --skip-smoke)
      SKIP_SMOKE=true
      shift
      ;;
    --project-root)
      PROJECT_ROOT="$2"
      shift 2
      ;;
    --standalone-app-name)
      STANDALONE_APP_NAME="$2"
      shift 2
      ;;
    --better-sqlite3-version)
      BETTER_SQLITE3_VERSION="$2"
      shift 2
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      fail "未知参数: $1"
      ;;
  esac
done

case "${SITE_MODE}" in
  standalone-root|site-community)
    ;;
  *)
    fail "site-mode 仅支持 standalone-root 或 site-community"
    ;;
esac

if [[ "${SKIP_BACKEND}" == true && "${SKIP_SITE}" == true ]]; then
  fail "skip-backend 和 skip-site 不能同时为 true"
fi

assert_command ssh
assert_command tar
assert_command pnpm

FRONTEND_ROOT="${PROJECT_ROOT}/frontend"
LOCK_PATH="${FRONTEND_ROOT}/pnpm-lock.yaml"
LOCK_BACKUP_PATH="$(mktemp)"
cp "${LOCK_PATH}" "${LOCK_BACKUP_PATH}"
TEMP_APP_PATH="${FRONTEND_ROOT}/apps/${STANDALONE_APP_NAME}"
trap cleanup EXIT

new_standalone_community_app() {
  local frontend_root="$1"
  local app_name="$2"
  local template_path="${frontend_root}/templates/standalone"
  local app_path="${frontend_root}/apps/${app_name}"

  rm -rf "${app_path}"
  cp -R "${template_path}" "${app_path}"

  APP_PACKAGE_PATH="${app_path}/package.json" APP_NAME_VALUE="${app_name}" node <<'EOF'
const fs = require('node:fs')
const packagePath = process.env.APP_PACKAGE_PATH
const appName = process.env.APP_NAME_VALUE
const pkg = JSON.parse(fs.readFileSync(packagePath, 'utf8'))
pkg.name = `@mortise/${appName}`
pkg.dependencies['@mortise/community-layer'] = 'workspace:*'
fs.writeFileSync(packagePath, `${JSON.stringify(pkg, null, 2)}\n`)
EOF

  cat > "${app_path}/app/app.config.ts" <<'EOF'
/**
 * 社区 standalone 部署配置
 */
export default defineAppConfig({
  community: {
    basePath: ''
  },
  ui: {
    colors: {
      primary: 'green',
      neutral: 'zinc'
    }
  }
})
EOF

  printf '%s\n' "${app_path}"
}

build_site_artifact() {
  local frontend_root="$1"
  local mode="$2"
  local app_name="$3"
  local old_node_options="${NODE_OPTIONS-}"
  export NODE_OPTIONS="--max-old-space-size=8192"

  if [[ "${mode}" == "standalone-root" ]]; then
    local app_path
    app_path="$(new_standalone_community_app "${frontend_root}" "${app_name}")"
    write_step "安装 standalone 社区发布工位依赖"
    (
      cd "${frontend_root}"
      pnpm install --filter "@mortise/${app_name}"
    )

    write_step "构建 standalone 社区前端"
    (
      cd "${frontend_root}"
      pnpm --filter "@mortise/${app_name}" build
    )

    if [[ -n "${old_node_options}" ]]; then
      export NODE_OPTIONS="${old_node_options}"
    else
      unset NODE_OPTIONS
    fi

    printf '%s\n' "${app_path}/.output"
    return
  fi

  write_step "构建 site-community 前端"
  (
    cd "${frontend_root}"
    pnpm --filter @mortise/site build
  )

  if [[ -n "${old_node_options}" ]]; then
    export NODE_OPTIONS="${old_node_options}"
  else
    unset NODE_OPTIONS
  fi

  printf '%s\n' "${frontend_root}/apps/site/.output"
}

SITE_ARTIFACT_PATH=""
if [[ "${SKIP_SITE}" != true ]]; then
  SITE_ARTIFACT_PATH="$(build_site_artifact "${FRONTEND_ROOT}" "${SITE_MODE}" "${STANDALONE_APP_NAME}")"
  [[ -d "${SITE_ARTIFACT_PATH}" ]] || fail "未找到待部署站点产物: ${SITE_ARTIFACT_PATH}"
fi

if [[ "${SKIP_BACKEND}" != true ]]; then
  write_step "部署 mortise-app"
  "${PROJECT_ROOT}/scripts/deploy-mortise-app.sh" --host "${DEPLOY_HOST}" --build --skip-smoke
fi

if [[ "${SKIP_SITE}" != true ]]; then
  timestamp="$(date +%Y%m%d%H%M%S)"
  site_backup_path="/opt/mortise/frontend/site/.output.bak-${timestamp}"
  artifact_parent="$(cd "$(dirname "${SITE_ARTIFACT_PATH}")" && pwd)"

  write_step "备份远端 site/.output"
  invoke_remote_bash "${DEPLOY_HOST}" \
    'set -e' \
    "if [ -d /opt/mortise/frontend/site/.output ]; then mv /opt/mortise/frontend/site/.output '${site_backup_path}'; fi"

  write_step "上传新站点产物到远端"
  tar -C "${artifact_parent}" -cf - .output | ssh "root@${DEPLOY_HOST}" 'cd /opt/mortise/frontend/site && tar -xf -'

  write_step "修复远端 better-sqlite3 原生依赖"
  invoke_remote_bash "${DEPLOY_HOST}" \
    'set -e' \
    "docker run --rm -v /opt/mortise/frontend/site/.output/server:/work node:22-slim bash -lc \"apt-get update >/dev/null && apt-get install -y python3 make g++ >/dev/null && mkdir -p /tmp/sqlitefix && cd /tmp/sqlitefix && npm init -y >/dev/null && npm install better-sqlite3@${BETTER_SQLITE3_VERSION} >/dev/null && rm -rf /work/node_modules/better-sqlite3 && cp -a /tmp/sqlitefix/node_modules/better-sqlite3 /work/node_modules/\""

  write_step "重建镜像并重启 mortise-site"
  invoke_remote_bash "${DEPLOY_HOST}" \
    'set -e' \
    'docker build -f /opt/mortise/frontend/Dockerfile.site -t mortise-site:latest /opt/mortise/frontend' \
    'docker compose -f /opt/mortise/docker-compose.app.yaml up -d mortise-site'

  write_step "站点回滚命令"
  cat <<EOF
ssh "root@${DEPLOY_HOST}" "rm -rf /opt/mortise/frontend/site/.output && mv ${site_backup_path} /opt/mortise/frontend/site/.output && docker build -f /opt/mortise/frontend/Dockerfile.site -t mortise-site:latest /opt/mortise/frontend && docker compose -f /opt/mortise/docker-compose.app.yaml up -d mortise-site"
EOF
fi

if [[ "${SKIP_SMOKE}" != true ]]; then
  write_step "执行社区 smoke 校验"
  "${PROJECT_ROOT}/scripts/smoke-community.sh" --host "${DEPLOY_HOST}" --site-mode "${SITE_MODE}"
fi
