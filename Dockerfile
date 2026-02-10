FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="rymcu.com"

ENV TZ=Asia/Shanghai

RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.tuna.tsinghua.edu.cn/g' /etc/apk/repositories && \
    apk add --no-cache tzdata

WORKDIR /app

RUN mkdir -p /logs/mortise/data

ARG JAR_FILE=mortise-app/target/mortise.jar
COPY ${JAR_FILE} /app/mortise.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/mortise.jar"]
