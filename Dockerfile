FROM eclipse-temurin:21-jdk-alpine

LABEL maintainer="rymcu.com"

# 设置时区为上海
ENV TZ=Asia/Shanghai

# 更换为清华大学的 Alpine 镜像源以加速，并安装必要的工具
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.tuna.tsinghua.edu.cn/g' /etc/apk/repositories && \
    apk add --no-cache curl tzdata

# 创建应用目录和日志目录
RUN mkdir -p /app /logs/mortise/data

MAINTAINER rymcu.com

RUN mkdir -p /logs/mortise

RUN rm -rf /usr/local/tomcat/webapps.dist

RUN rm -rf /usr/local/tomcat/webapps/*

COPY target/mortise.war /usr/local/tomcat/webapps/

EXPOSE 8080
