#!/bin/bash
# ============================================================
# install-docker.sh - 在 WSL 上安装 Docker 和 Docker Compose
# 适用: Debian / Ubuntu WSL2
# 使用方法: bash install-docker.sh
# ============================================================

set -e

echo "============================================"
echo "  Docker 环境检查与安装脚本"
echo "============================================"

# ========== 检查是否已有 Docker ==========
check_existing() {
    echo ""
    echo "[检查] 现有 Docker 环境..."

    if command -v docker &> /dev/null; then
        echo "  Docker: $(docker --version)"
        if docker ps &> /dev/null 2>&1; then
            echo "  ✅ Docker 可正常使用"
            DOCKER_OK=1
        else
            echo "  ⚠️  Docker 已安装但需要 sudo 或服务未启动"
            echo "  尝试启动..."
            sudo systemctl start docker 2>/dev/null || true
            if docker ps &> /dev/null 2>&1; then
                echo "  ✅ Docker 已修复"
                DOCKER_OK=1
            else
                DOCKER_OK=0
            fi
        fi
    else
        echo "  ❌ Docker 未安装"
        DOCKER_OK=0
    fi

    if command -v docker-compose &> /dev/null; then
        echo "  Docker Compose: $(docker-compose --version)"
        COMPOSE_OK=1
    elif docker compose version &> /dev/null 2>&1; then
        echo "  Docker Compose (plugin): 已安装"
        COMPOSE_OK=1
    else
        echo "  ❌ Docker Compose 未安装"
        COMPOSE_OK=0
    fi
}

# ========== 安装 Docker ==========
install_docker() {
    echo ""
    echo "[步骤 1/3] 安装 Docker..."

    # 卸载旧版本
    sudo apt-get remove -y -qq docker docker-engine docker.io containerd runc 2>/dev/null || true

    # 安装依赖
    sudo apt-get update -qq
    sudo apt-get install -y -qq ca-certificates curl gnupg

    # 添加 Docker 官方 GPG 密钥
    sudo install -m 0755 -d /etc/apt/keyrings
    curl -fsSL https://download.docker.com/linux/debian/gpg | \
        sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
    sudo chmod a+r /etc/apt/keyrings/docker.gpg

    # 添加 Docker 仓库
    echo \
      "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/debian \
      $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
      sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

    # 安装 Docker
    sudo apt-get update -qq
    sudo apt-get install -y -qq docker-ce docker-ce-cli containerd.io docker-buildx-plugin

    # 启动并启用
    sudo systemctl enable docker
    sudo systemctl start docker

    echo "  ✅ Docker 安装完成: $(docker --version)"
}

# ========== 安装 Docker Compose ==========
install_compose() {
    echo ""
    echo "[步骤 2/3] 安装 Docker Compose..."

    # 尝试安装 compose plugin
    sudo apt-get update -qq
    if sudo apt-get install -y -qq docker-compose-plugin; then
        echo "  ✅ Docker Compose 安装完成"

        # 创建 docker-compose 别名（兼容脚本）
        if ! command -v docker-compose &> /dev/null; then
            echo "  ℹ️  创建 docker-compose 兼容命令..."
            sudo tee /usr/local/bin/docker-compose << 'SCRIPT'
#!/bin/bash
exec docker compose "$@"
SCRIPT
            sudo chmod +x /usr/local/bin/docker-compose
        fi
    else
        echo "  ⚠️  Docker Compose plugin 安装失败，尝试手动安装..."
        COMPOSE_VERSION="v2.24.0"
        sudo curl -L "https://github.com/docker/compose/releases/download/${COMPOSE_VERSION}/docker-compose-$(uname -s)-$(uname -m)" \
            -o /usr/local/bin/docker-compose
        sudo chmod +x /usr/local/bin/docker-compose
        echo "  ✅ Docker Compose 手动安装完成"
    fi
}

# ========== 添加用户到 docker 组 ==========
setup_permissions() {
    echo ""
    echo "[步骤 3/3] 配置权限..."

    CURRENT_USER="${SUDO_USER:-$USER}"
    echo "  添加用户 '${CURRENT_USER}' 到 docker 组..."
    sudo usermod -aG docker "${CURRENT_USER}"

    echo "  ✅ 权限配置完成"
    echo ""
    echo "  ⚠️  需要重新登录或执行: newgrp docker"
    echo "  或直接退出当前 WSL 会话并重新进入"
}

# ========== 验证 ==========
verify() {
    echo ""
    echo "============================================"
    echo "  验证安装结果"
    echo "============================================"

    if command -v docker &> /dev/null && docker ps &> /dev/null; then
        echo "  ✅ Docker: $(docker --version)"
    else
        echo "  ❌ Docker 验证失败"
        return 1
    fi

    if command -v docker-compose &> /dev/null; then
        echo "  ✅ Docker Compose: $(docker-compose --version)"
    elif docker compose version &> /dev/null 2>&1; then
        echo "  ✅ Docker Compose (plugin): 已就绪"
    else
        echo "  ❌ Docker Compose 验证失败"
        return 1
    fi

    echo ""
    echo "  Docker 信息:"
    docker info --format '  OS: {{.OperatingSystem}} | 架构: {{.Architecture}} | 容器: {{.Containers}}'

    echo ""
    echo "============================================"
    echo "  ✅ 安装完成！"
    echo "============================================"
    echo ""
    echo "  下一步:"
    echo "  1. 退出并重新进入 WSL: exit"
    echo "  2. 克隆项目到 WSL: git clone git@github.com:ryanquan10/JAVA2026_IM_CT.git"
    echo "  3. 进入项目并运行: cd CT_IM_JAVA && bash docker/deploy.sh"
    echo ""
}

# ========== 主流程 ==========
main() {
    check_existing

    if [ "$DOCKER_OK" -eq 0 ]; then
        install_docker
    fi

    if [ "$COMPOSE_OK" -eq 0 ]; then
        install_compose
    fi

    setup_permissions
    verify
}

main "$@"
