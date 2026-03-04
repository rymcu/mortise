# syntax=docker/dockerfile:1

# ========== 构建阶段 ==========
FROM harbor.atdak.com/library/maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /workspace

COPY . .

# --mount=type=cache 将 .m2 持久化在宿主机 BuildKit 缓存中，跨次构建复用，无需额外配置
RUN --mount=type=cache,target=/root/.m2 \
    mvn -pl mortise-app -am clean package -DskipTests -B --no-transfer-progress

FROM harbor.atdak.com/library/eclipse-temurin:21-jre-alpine

LABEL maintainer="rymcu.com"

ENV TZ=Asia/Shanghai

RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.tuna.tsinghua.edu.cn/g' /etc/apk/repositories && \
    apk add --no-cache tzdata

WORKDIR /app

RUN mkdir -p /logs/mortise/data

COPY --from=builder /workspace/mortise-app/target/mortise.jar /app/mortise.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/mortise.jar"]
