#!/bin/bash
# ============================================================
# configure-docker-mirrors.sh - 配置 Docker 镜像加速
# 适用: WSL Debian / WSL Ubuntu-1
# 解决: Docker Hub EOF 错误
# ============================================================

set -e

echo "============================================"
echo "  配置 Docker 镜像加速"
echo "============================================"

DAEMON_JSON="/etc/docker/daemon.json"
SCRIPT_DIR="$(cd "$(dirname "$0")"; pwd)"
SOURCE_JSON="${SCRIPT_DIR}/daemon.json"

# 检查 Docker 是否安装
if ! command -v docker &> /dev/null; then
    echo "❌ Docker 未安装，请先运行 install-docker.sh"
    exit 1
fi

# 备份现有配置
if [ -f "$DAEMON_JSON" ]; then
    echo "📦 备份现有配置..."
    sudo cp "$DAEMON_JSON" "${DAEMON_JSON}.bak.$(date +%Y%m%d_%H%M%S)"
fi

# 复制新配置
echo "📝 写入镜像加速配置..."
sudo cp "$SOURCE_JSON" "$DAEMON_JSON"
sudo chmod 644 "$DAEMON_JSON"

# 重启 Docker
echo "🔄 重启 Docker 服务..."
sudo systemctl daemon-reload
sudo systemctl restart docker

# 验证配置
echo ""
echo "✅ 配置完成！当前镜像源:"
docker info 2>/dev/null | grep -A 5 "Registry Mirrors:" || echo "  (无法读取，但配置已写入)"

echo ""
echo "============================================"
echo "  测试拉取镜像: docker pull hello-world"
echo "============================================"
