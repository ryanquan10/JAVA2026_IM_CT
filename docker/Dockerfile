# ============================================================
# Dockerfile - CT_IM_JAVA
# 策略: 前端由 CI 提前构建 (npm run build)，Docker 只负责打包运行
# ============================================================

FROM swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/eclipse-temurin:17-jre-jammy

LABEL maintainer="SBQB-Group"
LABEL description="CT_IM_JAVA - Java IM Server + Vue Frontend"

RUN apt-get update -qq && apt-get install -y -qq curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# 前端构建产物（deploy.sh 复制到 docker/frontend-dist/）
COPY docker/frontend-dist/ ./page/

# 后端 jar（deploy.sh 复制到 docker/backend-jar/）
COPY docker/backend-jar/tio-site-all.jar ./lib/tio-site-all.jar

# 启动脚本
RUN echo '#!/bin/bash' > /app/run.sh && \
    echo 'APP_ENTRY=org.tio.sitexxx.all.Starter' >> /app/run.sh && \
    echo 'BASE=$(cd $(dirname \$0); pwd)' >> /app/run.sh && \
    echo 'CP=\${BASE}/config:\${BASE}/lib/*' >> /app/run.sh && \
    echo 'exec java -Xverify:none -Xms64m -Xmx2046m -Djava.util.Arrays.useLegacyMergeSort=true -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/java-tio-pid.hprof -cp \${CP} \${APP_ENTRY}' >> /app/run.sh && \
    chmod +x /app/run.sh

EXPOSE 8080

CMD ["/app/run.sh"]