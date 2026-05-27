#!/bin/bash
# ============================================================
# deploy.sh - CT_IM_JAVA 部署脚本
# 功能: 检查 Docker → 构建镜像 → 启动容器 → 健康检查
# 适用: WSL Debian / WSL Ubuntu-1
# 前提: 代码已由 GitHub Actions checkout 到当前目录
# ============================================================

set -e

PROJECT_DIR="$(cd "$(dirname "$0")/.."; pwd)"
DOCKER_DIR="${PROJECT_DIR}/docker"
HEALTH_URL="http://localhost:8080/healthz"

echo "============================================"
echo "  CT_IM_JAVA 部署脚本"
echo "  目录: ${PROJECT_DIR}"
echo "  时间: $(date '+%Y-%m-%d %H:%M:%S')"
echo "============================================"

# ========== 1. 检查 Docker ==========
check_docker() {
    echo ""
    echo "[1/5] 检查 Docker 环境..."

    if command -v docker &> /dev/null && docker ps &> /dev/null; then
        echo "  ✅ Docker: $(docker --version)"
    else
        echo "  ⚠️  Docker 不可用，尝试安装..."
        bash "$(dirname "$0")/install-docker.sh"
    fi

    # 兼容 docker-compose 和 docker compose (plugin)
    if docker compose version &> /dev/null 2>&1; then
        echo "  ✅ Docker Compose: 可用"
        DC="docker compose"
    elif command -v docker-compose &> /dev/null; then
        echo "  ✅ Docker Compose: $(docker-compose --version)"
        DC="docker-compose"
    else
        echo "  ❌ 未找到 Docker Compose"
        exit 1
    fi
}

# ========== 2. 使用 Artifact 产物构建镜像 ==========
build_image() {
    echo ""
    echo "[2/5] 构建 Docker 镜像..."

    cd "${PROJECT_DIR}"

    # 将 Actions artifact 产物放到 Docker 构建上下文
    # Actions download-artifact 会把文件放在 build-artifacts/
    echo "  准备构建产物..."

    # 查找后端 jar
    JAR_FILE=$(find build-artifacts -name "tio-site-all-*.jar" -path "*/target/*" 2>/dev/null | head -1)
    if [ -n "$JAR_FILE" ]; then
        echo "  ✅ 找到后端 JAR: $(basename "$JAR_FILE")"
        # 把 jar 复制到 docker/backend-jar/ 供 Dockerfile COPY
        mkdir -p "${DOCKER_DIR}/backend-jar"
        cp "$JAR_FILE" "${DOCKER_DIR}/backend-jar/tio-site-all.jar"
    else
        echo "  ⚠️  未找到预构建 JAR，将在 Docker 内 Maven 构建（较慢）"
        rm -rf "${DOCKER_DIR}/backend-jar"
    fi

    # 构建镜像
    ${DC} -f "${DOCKER_DIR}/docker-compose.yml" build

    # 清理临时构建目录
    rm -rf "${DOCKER_DIR}/backend-jar"

    echo "  ✅ 镜像构建完成"
}

# ========== 3. 部署容器 ==========
deploy() {
    echo ""
    echo "[3/5] 部署 Docker 容器..."

    cd "${PROJECT_DIR}"

    # 停止旧容器
    docker compose -f docker/docker-compose.yml down 2>/dev/null || true

    # 启动新容器
    echo "  启动容器..."
    docker compose -f docker/docker-compose.yml up -d

    echo "  ✅ 容器已启动"

    # 清理旧镜像
    echo ""
    echo "[4/5] 清理旧镜像..."
    docker image prune -f --filter "until=72h"
}

# ========== 4. 健康检查 ==========
health_check() {
    echo ""
    echo "[5/5] 健康检查..."

    echo "  等待服务启动（最多 120 秒）..."
    for i in $(seq 1 24); do
        if curl -sf "${HEALTH_URL}" &> /dev/null; then
            echo ""
            echo "  ✅ 服务健康检查通过！"
            echo ""
            echo "============================================"
            echo "  部署成功！"
            echo "  访问地址: http://$(hostname -I | awk '{print $1}'):8080"
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
check_docker
build_image
deploy
health_check
