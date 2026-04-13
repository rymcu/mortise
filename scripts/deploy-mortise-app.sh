#!/usr/bin/env bash

set -euo pipefail

DEPLOY_HOST="192.168.88.146"
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
JAR_PATH=""
BUILD=false
SKIP_SMOKE=false

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

usage() {
  cat <<'EOF'
用法:
  ./scripts/deploy-mortise-app.sh [options]

选项:
  --host <host>           目标主机，默认 192.168.88.146
  --project-root <path>   项目根目录，默认脚本上一级目录
  --jar-path <path>       指定待部署 jar 路径
  --build                 部署前执行 mvn -pl mortise-app -am clean package -DskipTests
  --skip-smoke            跳过健康检查
  -h, --help              显示帮助
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --host)
      DEPLOY_HOST="$2"
      shift 2
      ;;
    --project-root)
      PROJECT_ROOT="$2"
      shift 2
      ;;
    --jar-path)
      JAR_PATH="$2"
      shift 2
      ;;
    --build)
      BUILD=true
      shift
      ;;
    --skip-smoke)
      SKIP_SMOKE=true
      shift
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

assert_command ssh
assert_command scp

if [[ -z "${JAR_PATH}" ]]; then
  JAR_PATH="${PROJECT_ROOT}/mortise-app/target/mortise.jar"
fi

if [[ "${BUILD}" == true || ! -f "${JAR_PATH}" ]]; then
  assert_command mvn
  write_step "构建 mortise-app jar"
  (
    cd "${PROJECT_ROOT}"
    mvn -pl mortise-app -am clean package -DskipTests
  )
fi

[[ -f "${JAR_PATH}" ]] || fail "未找到待部署 jar: ${JAR_PATH}"

timestamp="$(date +%Y%m%d%H%M%S)"
backup_path="/opt/mortise/mortise.jar.bak-${timestamp}"

write_step "备份线上 mortise.jar"
invoke_remote_bash "${DEPLOY_HOST}" \
  'set -e' \
  "cp /opt/mortise/mortise.jar '${backup_path}'"

write_step "上传新 mortise.jar"
scp "${JAR_PATH}" "root@${DEPLOY_HOST}:/opt/mortise/mortise.jar"

write_step "重建镜像并重启 mortise-app"
invoke_remote_bash "${DEPLOY_HOST}" \
  'set -e' \
  'docker build -f /opt/mortise/Dockerfile.runtime -t mortise-app:latest /opt/mortise' \
  'docker compose -f /opt/mortise/docker-compose.app.yaml up -d mortise-app'

if [[ "${SKIP_SMOKE}" != true ]]; then
  write_step "校验后端健康状态"
  invoke_remote_bash "${DEPLOY_HOST}" \
    'set -e' \
    'curl -fsS http://127.0.0.1:9999/mortise/actuator/health' \
    'docker logs --tail 20 mortise-app 2>&1 || true'
fi

write_step "回滚命令"
cat <<EOF
scp "root@${DEPLOY_HOST}:${backup_path}" "${JAR_PATH}"
scp "${JAR_PATH}" "root@${DEPLOY_HOST}:/opt/mortise/mortise.jar"
ssh "root@${DEPLOY_HOST}" "docker build -f /opt/mortise/Dockerfile.runtime -t mortise-app:latest /opt/mortise && docker compose -f /opt/mortise/docker-compose.app.yaml up -d mortise-app"
EOF
