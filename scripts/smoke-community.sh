#!/usr/bin/env bash

set -euo pipefail

DEPLOY_HOST="192.168.88.146"
SITE_MODE="standalone-root"

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

assert_http_200() {
  local uri="$1"
  shift || true
  local args=()
  local retry_count=10
  local retry_delay=5
  while [[ $# -gt 0 ]]; do
    args+=("$1")
    shift
  done

  local status
  local attempt
  for ((attempt=1; attempt<=retry_count; attempt++)); do
    status="$(curl -sS -o /dev/null -w '%{http_code}' "${args[@]}" "${uri}")"
    if [[ "${status}" == "200" ]]; then
      printf '%s => 200\n' "${uri}"
      return
    fi

    if [[ ${attempt} -lt ${retry_count} ]]; then
      printf '%s => %s，等待 %s 秒后重试 (%s/%s)\n' "${uri}" "${status}" "${retry_delay}" "${attempt}" "${retry_count}"
      sleep "${retry_delay}"
      continue
    fi

    fail "接口校验失败: ${uri} => ${status}"
  done
}

usage() {
  cat <<'EOF'
用法:
  ./scripts/smoke-community.sh [options]

选项:
  --host <host>        目标主机，默认 192.168.88.146
  --site-mode <mode>   standalone-root 或 site-community
  -h, --help           显示帮助
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
  standalone-root)
    SITE_PATHS=("/topics" "/collections")
    ;;
  site-community)
    SITE_PATHS=("/community" "/community/collections")
    ;;
  *)
    fail "site-mode 仅支持 standalone-root 或 site-community"
    ;;
esac

assert_command ssh
assert_command curl

write_step "校验后端健康状态"
invoke_remote_bash "${DEPLOY_HOST}" \
  'set -e' \
  'curl -fsS http://127.0.0.1:9999/mortise/actuator/health'

write_step "校验前台业务路由"
for path in "${SITE_PATHS[@]}"; do
  assert_http_200 "http://${DEPLOY_HOST}${path}"
done

write_step "检查容器状态与最近日志"
invoke_remote_bash "${DEPLOY_HOST}" \
  'set -e' \
  'for name in mortise-site mortise-app; do' \
  '  running=$(docker inspect -f "{{.State.Running}}" "$name")' \
  '  status=$(docker inspect -f "{{.State.Status}}" "$name")' \
  '  restart_count=$(docker inspect -f "{{.RestartCount}}" "$name")' \
  '  echo "$name status=$status restartCount=$restart_count"' \
  '  if [ "$running" != "true" ]; then' \
  '    echo "$name 未处于运行态" >&2' \
  '    exit 1' \
  '  fi' \
  'done' \
  'site_logs=$(docker logs --tail 80 mortise-site 2>&1 || true)' \
  'app_logs=$(docker logs --tail 80 mortise-app 2>&1 || true)' \
  'echo "$site_logs"' \
  'printf "\n---APP---\n"' \
  'echo "$app_logs"' \
  'printf "%s\n%s\n" "$site_logs" "$app_logs" | grep -E "ERR_DLOPEN_FAILED|better_sqlite3\.node|Module did not self-register" >/dev/null && exit 1 || true'

AUTH_ARGS=()
if [[ -n "${MORTISE_ADMIN_BEARER_TOKEN:-}" ]]; then
  token="${MORTISE_ADMIN_BEARER_TOKEN}"
  if [[ ! "${token}" =~ ^[Bb]earer[[:space:]] ]]; then
    token="Bearer ${token}"
  fi
  AUTH_ARGS=(-H "Authorization: ${token}")
elif [[ -n "${MORTISE_ADMIN_COOKIE:-}" ]]; then
  AUTH_ARGS=(-H "Cookie: ${MORTISE_ADMIN_COOKIE}")
fi

if [[ ${#AUTH_ARGS[@]} -eq 0 ]]; then
  write_step "未提供管理端认证信息，跳过 dashboard 接口校验"
  printf '如需校验管理端接口，请设置环境变量 MORTISE_ADMIN_BEARER_TOKEN 或 MORTISE_ADMIN_COOKIE\n'
  exit 0
fi

write_step "校验管理端社区看板接口"
assert_http_200 "http://${DEPLOY_HOST}/mortise/api/v1/admin/community/dashboard/overview" "${AUTH_ARGS[@]}"
assert_http_200 "http://${DEPLOY_HOST}/mortise/api/v1/admin/community/dashboard/trends" "${AUTH_ARGS[@]}"
assert_http_200 "http://${DEPLOY_HOST}/mortise/api/v1/admin/community/dashboard/pending" "${AUTH_ARGS[@]}"
