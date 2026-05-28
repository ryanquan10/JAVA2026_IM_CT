#!/bin/bash
# ============================================================
# deploy.sh - CT_IM_JAVA 全自动部署脚本
# 功能: 装 Docker → 拉代码 → 构建镜像 → 启动容器 → 健康检查
# 适用: WSL Debian / WSL Ubuntu-1
# 可作为 GitHub Actions 的 deploy 步骤直接调用
# ============================================================

set -e

PROJECT_DIR="$(cd "$(dirname "$0")/.."; pwd)"
DEPLOY_USER="${DEPLOY_USER:-debianuser}"
HEALTH_URL="http://localhost:8080/healthz"

echo "============================================"
echo "  CT_IM_JAVA 部署脚本"
echo "  目录: ${PROJECT_DIR}"
echo "  时间: $(date '+%Y-%m-%d %H:%M:%S')"
echo "============================================"

# ========== 1. 检查并安装 Docker ==========
check_docker() {
    echo ""
    echo "[1/6] 检查 Docker 环境..."

    if command -v docker &> /dev/null && docker ps &> /dev/null 2>&1; then
        echo "  ✅ Docker: $(docker --version)"
    else
        echo "  ⚠️  Docker 未安装或不可用，开始安装..."
        install_docker
    fi

    # 兼容 docker-compose 和 docker compose (plugin)
    if docker compose version &> /dev/null 2>&1; then
        echo "  ✅ Docker Compose: 可用"
        DC="docker compose"
    elif command -v docker-compose &> /dev/null; then
        echo "  ✅ Docker Compose: $(docker-compose --version)"
        DC="docker-compose"
    else
        echo "  ⚠️  Docker Compose 未安装，安装中..."
        install_docker_compose
        if docker compose version &> /dev/null 2>&1; then
            DC="docker compose"
        elif command -v docker-compose &> /dev/null; then
            DC="docker-compose"
        else
            echo "  ❌ Docker Compose 安装失败"
            exit 1
        fi
    fi
}

install_docker() {
    echo ""
    echo "  [安装 Docker]"

    # 检测发行版
    if [ -f /etc/os-release ]; then
        . /etc/os-release
        OS=$ID
    else
        OS="debian"
    fi

    echo "  检测到发行版: ${OS}"

    # 安装依赖
    sudo apt-get update -qq 2>/dev/null || apt-get update -qq
    sudo apt-get install -y -qq ca-certificates curl gnupg 2>/dev/null || apt-get install -y -qq ca-certificates curl gnupg

    # 添加 Docker 官方 GPG 密钥
    sudo install -m 0755 -d /etc/apt/keyrings 2>/dev/null || install -m 0755 -d /etc/apt/keyrings
    curl -fsSL https://download.docker.com/linux/${OS}/gpg | \
        sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg 2>/dev/null || gpg --dearmor -o /etc/apt/keyrings/docker.gpg
    sudo chmod a+r /etc/apt/keyrings/docker.gpg 2>/dev/null || chmod a+r /etc/apt/keyrings/docker.gpg

    # 添加 Docker 仓库
    CODENAME=$(. /etc/os-release 2>/dev/null && echo "$VERSION_CODENAME" || echo "bullseye")
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/${OS} ${CODENAME} stable" | \
        sudo tee /etc/apt/sources.list.d/docker.list > /dev/null 2>/dev/null

    sudo apt-get update -qq 2>/dev/null || apt-get update -qq
    sudo apt-get install -y -qq docker-ce docker-ce-cli containerd.io docker-buildx-plugin 2>/dev/null || \
        apt-get install -y -qq docker-ce docker-ce-cli containerd.io docker-buildx-plugin

    # 启动并启用
    sudo systemctl enable docker 2>/dev/null || true
    sudo systemctl start docker 2>/dev/null || true

    # 添加当前用户到 docker 组
    CURRENT_USER=$(whoami)
    sudo usermod -aG docker "$CURRENT_USER" 2>/dev/null || true

    echo "  ✅ Docker 安装完成: $(docker --version)"
}

install_docker_compose() {
    echo ""
    echo "  [安装 Docker Compose]"
    sudo apt-get update -qq 2>/dev/null || apt-get update -qq
    sudo apt-get install -y -qq docker-compose-plugin 2>/dev/null || apt-get install -y -qq docker-compose-plugin
    echo "  ✅ Docker Compose 安装完成"
}

# ========== 2. 克隆/更新代码 ==========
update_code() {
    echo ""
    echo "[2/6] 检查代码..."

    # 如果当前目录已经是项目根目录（有 .github/workflows），就用当前目录
    if [ -f "${PROJECT_DIR}/.github/workflows/deploy.yml" ]; then
        echo "  ✅ 代码已在: ${PROJECT_DIR}"
        cd "${PROJECT_DIR}"
    elif [ -d "${PROJECT_DIR}/.git" ]; then
        echo "  🔄 更新现有代码..."
        cd "${PROJECT_DIR}"
        git fetch origin 2>/dev/null || true
        git pull origin main 2>/dev/null || git pull origin master 2>/dev/null || true
    else
        echo "  📦 首次克隆代码..."
        REPO_URL="git@github.com:ryanquan10/JAVA2026_IM_CT.git"
        if [ ! -d "$(dirname "${PROJECT_DIR}")" ]; then
            mkdir -p "$(dirname "${PROJECT_DIR}")"
        fi
        # 尝试 SSH，失败则用 HTTPS
        if git ls-remote --exit-code ${REPO_URL} &>/dev/null; then
            git clone ${REPO_URL} "${PROJECT_DIR}"
        else
            echo "  ⚠️  SSH 不可用，尝试 HTTPS..."
            git clone https://github.com/ryanquan10/JAVA2026_IM_CT.git "${PROJECT_DIR}"
        fi
        cd "${PROJECT_DIR}"
    fi

    echo "  ✅ 代码已就绪: $(git rev-parse --short HEAD 2>/dev/null || echo 'unknown')"
}

# ========== 3. 构建 Docker 镜像 ==========
build_image() {
    echo ""
    echo "[3/6] 构建 Docker 镜像..."

    cd "${PROJECT_DIR}"

    # 准备构建产物目录
    BUILD_DIR="${PROJECT_DIR}/docker"
    mkdir -p "${BUILD_DIR}/backend-jar"
    mkdir -p "${BUILD_DIR}/frontend-dist"

    # 查找 artifact 产物（GitHub Actions 会在 deploy job 的 _work 目录中）
    # 可能的位置：
    #   - 当前目录下的 build-artifacts/（手动下载后）
    #   - GitHub Actions 的工作目录
    WORK_DIR="${RUNNER_WORK_DIR:-}"
    if [ -n "$WORK_DIR" ] && [ -d "$WORK_DIR" ]; then
        echo "  检测到 Runner 工作目录: $WORK_DIR"
        ARTIFACT_DIR=$(find "$WORK_DIR" -name "build-*" -type d 2>/dev/null | head -1)
        if [ -n "$ARTIFACT_DIR" ]; then
            echo "  找到构建产物: $ARTIFACT_DIR"
            JAR_FILE=$(find "$ARTIFACT_DIR" -name "tio-site-all-*.jar" 2>/dev/null | head -1)
            if [ -n "$JAR_FILE" ]; then
                cp "$JAR_FILE" "${BUILD_DIR}/backend-jar/tio-site-all.jar"
                echo "  ✅ 使用 CI 预构建 JAR"
            fi
            DIST_DIR=$(find "$ARTIFACT_DIR" -name "dist" -type d 2>/dev/null | head -1)
            if [ -n "$DIST_DIR" ]; then
                cp -r "$DIST_DIR"/* "${BUILD_DIR}/frontend-dist/" 2>/dev/null || true
                echo "  ✅ 使用 CI 预构建前端"
            fi
        fi
    fi

    # 如果没用 CI 产物，就完整构建
    if [ ! -f "${BUILD_DIR}/backend-jar/tio-site-all.jar" ]; then
        echo "  ⚠️  无 CI 产物，执行本地构建（较慢，约 10-15 分钟）..."

        # 构建后端
        echo "  [Maven 构建后端]..."
        cd "${PROJECT_DIR}/bs-server/all"
        mvn clean package -P linux -DskipTests -Dmaven.javadoc.skip=true -B 2>&1 | tail -5
        JAR_FILE=$(find target -name "tio-site-all-*.jar" 2>/dev/null | head -1)
        if [ -n "$JAR_FILE" ]; then
            mkdir -p "${BUILD_DIR}/backend-jar"
            cp "$JAR_FILE" "${BUILD_DIR}/backend-jar/tio-site-all.jar"
        fi

        # 构建前端
        echo "  [npm 构建前端]..."
        cd "${PROJECT_DIR}/mg-page"
        npm ci --silent 2>&1 | tail -3
        npm run build 2>&1 | tail -5
        if [ -d "dist" ]; then
            mkdir -p "${BUILD_DIR}/frontend-dist"
            cp -r dist/* "${BUILD_DIR}/frontend-dist/"
        fi

        cd "${PROJECT_DIR}"
    fi

    # Docker 构建
    cd "${PROJECT_DIR}"
    ${DC} -f "${BUILD_DIR}/docker-compose.yml" build

    # 清理临时目录
    rm -rf "${BUILD_DIR}/backend-jar" "${BUILD_DIR}/frontend-dist"

    echo "  ✅ 镜像构建完成"
}

# ========== 4. 部署容器 ==========
deploy() {
    echo ""
    echo "[4/6] 部署 Docker 容器..."

    cd "${PROJECT_DIR}"

    ${DC} -f docker/docker-compose.yml down 2>/dev/null || true

    echo "  启动容器..."
    ${DC} -f docker/docker-compose.yml up -d

    echo "  ✅ 容器已启动"

    echo ""
    echo "[5/6] 清理旧镜像..."
    docker image prune -f --filter "until=72h" 2>/dev/null || true
    echo "  ✅ 清理完成"
}

# ========== 5. 健康检查 ==========
health_check() {
    echo ""
    echo "[6/6] 健康检查..."

    echo "  等待服务启动（最多 120 秒）..."
    for i in $(seq 1 24); do
        if curl -sf "${HEALTH_URL}" &> /dev/null; then
            echo ""
            echo "  ✅ 服务健康检查通过！"
            echo ""
            echo "============================================"
            echo "  部署成功！"
            echo "  访问地址: http://$(hostname -I | awk '{print $1}'):8080"
            echo "  健康检查: ${HEALTH_URL}"
            echo "============================================"
            return 0
        fi
        printf "  等待中... (%d/24)\n" "$i"
        sleep 5
    done

    echo ""
    echo "  ⚠️  健康检查超时，查看日志:"
    echo "  docker logs ct-im-java"
    echo ""
    echo "============================================"
    echo "  部署完成（可能仍在启动中）"
    echo "============================================"
    return 1
}

# ========== 主流程 ==========
main() {
    check_docker
    update_code
    build_image
    deploy
    health_check
}

main "$@"
