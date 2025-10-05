FROM eclipse-temurin:21-jdk-alpine

LABEL maintainer="rymcu.com"

# 设置时区为上海
ENV TZ=Asia/Shanghai

# 更换为清华大学的 Alpine 镜像源以加速，并安装必要的工具
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.tuna.tsinghua.edu.cn/g' /etc/apk/repositories && \
    apk add --no-cache curl tzdata

# 创建应用目录和日志目录
RUN mkdir -p /app /logs/mortise/data

# 设置工作目录
WORKDIR /app

# 复制 JAR 文件
COPY mortise-app/target/mortise.jar app.jar

# 创建非 root 用户
RUN addgroup -S spring && adduser -S spring -G spring
RUN chown -R spring:spring /app /logs
USER spring

# 设置环境变量
ENV SPRING_PROFILES_ACTIVE=prod
ENV TZ=Asia/Shanghai

# JVM 优化参数
ENV JAVA_OPTS="-Xms512m -Xmx2048m \
    -Djava.awt.headless=true \
    -Dfile.encoding=UTF-8 \
    -Duser.timezone=Asia/Shanghai \
    -Djava.security.egd=file:/dev/./urandom \
    -XX:+UseG1GC \
    -XX:+UseStringDeduplication \
    -XX:+OptimizeStringConcat"

# 暴露端口
EXPOSE 9999

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:9999/mortise/actuator/health || exit 1

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
