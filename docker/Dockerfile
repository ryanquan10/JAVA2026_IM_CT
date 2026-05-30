# ============================================================
# Dockerfile - CT_IM_JAVA
# 策略: 前端在 Docker 内构建, jar 由 CI 外部传入
# ============================================================

# ===== 阶段1: 构建 Vue.js 前端 =====
FROM node:18-alpine AS frontend-builder

WORKDIR /build/mg-page
COPY mg-page/package*.json ./
RUN npm ci --silent
COPY mg-page/ ./
RUN npm run build

# ===== 阶段2: 最终镜像 =====
FROM openjdk:17-jre-alpine

LABEL maintainer="SBQB-Group"
LABEL description="CT_IM_JAVA - Java IM Server + Vue Frontend"

RUN apk add --no-cache curl dumb-init

WORKDIR /app

# 前端构建产物
COPY --from=frontend-builder /build/mg-page/dist/ ./page/

# 后端 jar —— deploy.sh 会在 docker-compose build 前
# 把 artifact 复制到 docker/backend-jar/tio-site-all.jar
COPY docker/backend-jar/tio-site-all.jar ./lib/tio-site-all.jar

# 启动脚本
RUN echo '#!/bin/bash' > /app/run.sh && \
    echo 'APP_ENTRY=org.tio.sitexxx.all.Starter' >> /app/run.sh && \
    echo 'BASE=$(cd $(dirname \$0); pwd)' >> /app/run.sh && \
    echo 'CP=\${BASE}/config:\${BASE}/lib/*' >> /app/run.sh && \
    echo 'exec java -Xverify:none -Xms64m -Xmx2046m -Djava.util.Arrays.useLegacyMergeSort=true -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/java-tio-pid.hprof -cp \${CP} \${APP_ENTRY}' >> /app/run.sh && \
    chmod +x /app/run.sh

EXPOSE 8080

ENTRYPOINT ["/usr/bin/dumb-init", "--"]
CMD ["/app/run.sh"]
