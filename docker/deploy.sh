#!/bin/bash
# ============================================================
# deploy.sh - CT_IM_JAVA 全自动部署脚本
# 功能: 装 Docker → 拉代码 → 构建镜像 → 启动容器 → 健康检查
# 适用: WSL Debian / WSL Ubuntu-1
# 可作为 GitHub Actions 的 deploy 步骤直接调用
# ============================================================

set -eo pipefail

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

    if [ -f /etc/os-release ]; then
        . /etc/os-release
        OS=$ID
    else
        OS="debian"
    fi

    echo "  检测到发行版: ${OS}"

    # 设置代理
    export https_proxy=http://198.18.0.1:7890
    export http_proxy=http://198.18.0.1:7890
    export HTTPS_PROXY=http://198.18.0.1:7890
    export HTTP_PROXY=http://198.18.0.1:7890

    # 安装依赖
    sudo apt-get update -qq
    sudo apt-get install -y -qq ca-certificates curl gnupg

    # 添加 Docker GPG 密钥（非交互式）
    sudo install -m 0755 -d /etc/apt/keyrings
    curl --insecure -x http://198.18.0.1:7890 -fsSL https://download.docker.com/linux/${OS}/gpg | \
        sudo gpg --batch --yes --dearmor -o /etc/apt/keyrings/docker.gpg
    sudo chmod a+r /etc/apt/keyrings/docker.gpg

    # 添加仓库
    CODENAME=$(. /etc/os-release && echo "$VERSION_CODENAME")
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/${OS} ${CODENAME} stable" | \
        sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

    sudo apt-get update -qq
    sudo apt-get install -y -qq docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

    sudo systemctl enable docker
    sudo systemctl start docker
    sudo usermod -aG docker "$(whoami)" || true

    echo "  ✅ Docker 安装完成: $(docker --version)"
}

install_docker_compose() {
    echo ""
    echo "  [安装 Docker Compose]"
    sudo apt-get update -qq 2>/dev/null || apt-get update -qq
    sudo apt-get install -y -qq docker-compose-plugin 2>/dev/null || apt-get install -y -qq docker-compose-plugin
    echo "  ✅ Docker Compose 安装完成"
}

find_backend_jar() {
    find "$1" -type f \( -name "tio-site-all.jar" -o -name "tio-site-all-*.jar" \) \
        ! -path "*/archive-tmp/*" ! -path "*/tio-site-all/lib/*" \
        ! -name "*sources.jar" ! -name "*javadoc.jar" -print -quit 2>/dev/null || true
}

find_frontend_dist() {
    local root="$1"
    local preferred=""
    local candidate

    preferred="$(find "$root" -type d -path "*/mg-page/dist" -print -quit 2>/dev/null || true)"
    if [ -n "$preferred" ] && [ -f "${preferred}/index.html" ]; then
        printf '%s\n' "$preferred"
        return 0
    fi

    while IFS= read -r candidate; do
        if [ -f "${candidate}/index.html" ]; then
            printf '%s\n' "$candidate"
            return 0
        fi
    done < <(find "$root" -type d -name dist 2>/dev/null)
}

copy_build_artifacts() {
    local artifact_dir="$1"
    local build_dir="$2"
    local jar_file=""
    local dist_dir=""

    if [ -z "$artifact_dir" ] || [ ! -d "$artifact_dir" ]; then
        return 1
    fi

    echo "  查找构建产物: ${artifact_dir}"
    jar_file="$(find_backend_jar "$artifact_dir")"
    dist_dir="$(find_frontend_dist "$artifact_dir")"

    if [ -z "$jar_file" ] || [ -z "$dist_dir" ]; then
        echo "  ⚠️  该目录缺少 JAR 或前端 dist"
        return 1
    fi

    mkdir -p "${build_dir}/backend-jar" "${build_dir}/frontend-dist"
    cp "$jar_file" "${build_dir}/backend-jar/tio-site-all.jar"
    cp -r "${dist_dir}/." "${build_dir}/frontend-dist/"

    echo "  ✅ 使用 CI 预构建 JAR: ${jar_file}"
    echo "  ✅ 使用 CI 预构建前端: ${dist_dir}"
    return 0
}

ensure_local_build_tools() {
    local missing=0
    local node_bin=""
    local npm_bin=""

    if [ -z "${JAVA_HOME:-}" ] || [ ! -x "${JAVA_HOME}/bin/java" ]; then
        echo "  ❌ JAVA_HOME 未正确设置: ${JAVA_HOME:-<empty>}"
        missing=1
    fi

    if ! command -v mvn >/dev/null 2>&1; then
        echo "  ❌ Maven 未安装或不在 PATH"
        missing=1
    fi

    node_bin="$(command -v node 2>/dev/null || true)"
    npm_bin="$(command -v npm 2>/dev/null || true)"
    if [ -z "$node_bin" ]; then
        echo "  ❌ Node.js 未安装或不在 PATH"
        missing=1
    elif printf '%s' "$node_bin" | grep -Eqi '(^/mnt/|\.exe$|\.cmd$|\.bat$)'; then
        echo "  ❌ node 指向 Windows 可执行文件: ${node_bin}"
        missing=1
    fi

    if [ -z "$npm_bin" ]; then
        echo "  ❌ npm 未安装或不在 PATH"
        missing=1
    elif printf '%s' "$npm_bin" | grep -Eqi '(^/mnt/|\.exe$|\.cmd$|\.bat$)'; then
        echo "  ❌ npm 指向 Windows 可执行文件: ${npm_bin}"
        missing=1
    fi

    if [ "$missing" -ne 0 ]; then
        echo "  ❌ 未找到可用 CI 产物，且本地构建环境不完整。"
        echo "     GitHub Actions 部署应先下载 build artifact，并设置 DEPLOY_ARTIFACT_DIR。"
        exit 1
    fi
}

install_local_maven_deps() {
    if [ -f "${PROJECT_DIR}/lib/aplus-captcha-10.8.8.jar" ]; then
        mvn install:install-file -Dfile="${PROJECT_DIR}/lib/aplus-captcha-10.8.8.jar" -DgroupId=org.aplus -DartifactId=aplus-captcha -Dversion=10.8.8 -Dpackaging=jar
    fi

    if [ -f "${PROJECT_DIR}/lib/5upay-sdk-core-1.0.0.jar" ]; then
        mvn install:install-file -Dfile="${PROJECT_DIR}/lib/5upay-sdk-core-1.0.0.jar" -DgroupId=org.t-io -DartifactId=5upay-sdk-core -Dversion=1.0.0 -Dpackaging=jar
    fi
}

# ========== 2. 克隆/更新代码 ==========
update_code() {
    echo ""
    echo "[2/6] 检查代码..."

    # 如果当前目录已经是项目根目录（有 .github/workflows），先 pull 最新代码
    if [ -f "${PROJECT_DIR}/.github/workflows/deploy.yml" ]; then
        echo "  🔄 更新代码..."
        cd "${PROJECT_DIR}"
        git fetch origin 2>/dev/null || true
        git pull origin main 2>/dev/null || git pull origin master 2>/dev/null || true
        echo "  ✅ 代码已就绪: $(git rev-parse --short HEAD 2>/dev/null || echo 'unknown')"
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
    rm -rf "${BUILD_DIR}/backend-jar" "${BUILD_DIR}/frontend-dist"
    mkdir -p "${BUILD_DIR}/backend-jar"
    mkdir -p "${BUILD_DIR}/frontend-dist"

    # 查找 GitHub Actions 下载的 artifact 产物。workflow 会显式设置 DEPLOY_ARTIFACT_DIR；
    # 其他目录用于兼容手动下载或旧 runner 环境。
    ARTIFACTS_READY=0
    ARTIFACT_CANDIDATES=()
    if [ -n "${DEPLOY_ARTIFACT_DIR:-}" ]; then
        ARTIFACT_CANDIDATES+=("${DEPLOY_ARTIFACT_DIR}")
    fi
    if [ -d "${PROJECT_DIR}/build-artifacts" ]; then
        ARTIFACT_CANDIDATES+=("${PROJECT_DIR}/build-artifacts")
    fi
    if [ -n "${RUNNER_TEMP:-}" ] && [ -d "${RUNNER_TEMP}/ct-im-java-artifacts" ]; then
        ARTIFACT_CANDIDATES+=("${RUNNER_TEMP}/ct-im-java-artifacts")
    fi
    if [ -n "${GITHUB_WORKSPACE:-}" ] && [ -d "${GITHUB_WORKSPACE}" ]; then
        ARTIFACT_CANDIDATES+=("${GITHUB_WORKSPACE}")
    fi
    if [ -n "${RUNNER_WORKSPACE:-}" ] && [ -d "${RUNNER_WORKSPACE}" ]; then
        ARTIFACT_CANDIDATES+=("${RUNNER_WORKSPACE}")
    fi

    for ARTIFACT_DIR in "${ARTIFACT_CANDIDATES[@]}"; do
        if copy_build_artifacts "$ARTIFACT_DIR" "$BUILD_DIR"; then
            ARTIFACTS_READY=1
            break
        fi
    done

    # 如果没用 CI 产物，就完整构建
    if [ "$ARTIFACTS_READY" -eq 0 ]; then
        echo "  ⚠️  无 CI 产物，执行本地构建（较慢，约 10-15 分钟）..."
        ensure_local_build_tools

        echo "  [安装本地 Maven 依赖]..."
        install_local_maven_deps

        echo "  [Maven install tio 模块]..."
        cd "${PROJECT_DIR}/tio/src/parent"
        mvn clean install -DskipTests -Dmaven.javadoc.skip=true -B -e -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Dmaven.resolver.transport=wagon

        echo "  [Maven install 业务模块]..."
        cd "${PROJECT_DIR}/bs-server/parent"
        mvn clean install -DskipTests -Dmaven.javadoc.skip=true -B -e -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Dmaven.resolver.transport=wagon

        # 打包最终 jar
        echo "  [Maven 打包]..."
        cd "${PROJECT_DIR}/bs-server/all"
        mvn clean package -P linux -DskipTests -Dmaven.javadoc.skip=true -B -e -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Dmaven.resolver.transport=wagon
        JAR_FILE="$(find_backend_jar "${PROJECT_DIR}/bs-server/all/target")"
        if [ -n "$JAR_FILE" ]; then
            cp "$JAR_FILE" "${BUILD_DIR}/backend-jar/tio-site-all.jar"
        else
            echo "  ❌ Maven 打包完成但未找到 tio-site-all.jar"
            exit 1
        fi

        # 构建前端
        echo "  [npm 构建前端]..."
        cd "${PROJECT_DIR}/mg-page"
        npm ci --legacy-peer-deps
        npm run build
        if [ -f "dist/index.html" ]; then
            cp -r dist/. "${BUILD_DIR}/frontend-dist/"
        else
            echo "  ❌ npm 构建完成但未找到 mg-page/dist/index.html"
            exit 1
        fi

        cd "${PROJECT_DIR}"
    fi

    if [ ! -s "${BUILD_DIR}/backend-jar/tio-site-all.jar" ]; then
        echo "  ❌ Docker 构建输入缺少后端 JAR: ${BUILD_DIR}/backend-jar/tio-site-all.jar"
        exit 1
    fi
    if [ ! -f "${BUILD_DIR}/frontend-dist/index.html" ]; then
        echo "  ❌ Docker 构建输入缺少前端 dist: ${BUILD_DIR}/frontend-dist/index.html"
        exit 1
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
