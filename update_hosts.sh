#!/bin/bash

# Check if running as root (兼容写法)
if [ "$(id -u)" -ne 0 ]; then
  echo "Please run this script with root privilege or sudo" >&2
  exit 1
fi

# 设置 hosts 文件路径
hosts_path="/etc/hosts"

# 自动判断系统类型
os_type=""
case "$(uname -s)" in
  Linux*)   os_type="Linux" ;;
  Darwin*)  os_type="macOS" ;;
  *)        echo "Unsupported operating systems" >&2; exit 1 ;;
esac

# 获取本机 192.168 IP 系统兼容版 (核心差异部分)
get_local_ip() {
  if [ "$os_type" = "Linux" ]; then
    # Linux 获取方法 (适配多网卡情况)
    hostname -I | awk '{ for(i=1;i<=NF;i++) if($i ~ /^192\.168/) {print $i; exit}}'
  else
    # macOS 获取方法 (优先无线网卡 en0)
    ifconfig en0 | awk '/inet / && /192\.168/ {print $2; exit}'
    [ -z "$local_ip" ] && ifconfig | awk '/inet / && /192\.168/ {print $2; exit}'
  fi
}

# 获取 IP 地址并验证
local_ip=$(get_local_ip)
if [ -z "$local_ip" ]; then
  echo "Error: 192.168.x.x LAN IP not detected" >&2
  echo "Operating System: $os_type" >&2
  [ "$os_type" = "macOS" ] && echo "Tip: If you're using a USB tether, try switching to an EN1 port" >&2
  exit 1
fi

# 域名配置
domains=(
  "$local_ip rymcu.local"
  "$local_ip logto.rymcu.local"
  "$local_ip auth.rymcu.local"
  "$local_ip npm.rymcu.local"
)

# 添加域名函数 (兼容 BSD/linux 的 grep 差异)
add_domain() {
  local domain="$1"
  if grep -q "^[[:space:]]*${domain//./\\.}[[:space:]]*$" "$hosts_path"; then
    echo "already exists: $domain"
  else
    echo "Adding: $domain"
    echo "$domain" >> "$hosts_path"
  fi
}

# 执行添加操作
echo "Updating the hosts file (system type: $os_type)"
for domain in "${domains[@]}"; do
  add_domain "$domain"
done

echo "The operation is complete, use IP: $local_ip"
