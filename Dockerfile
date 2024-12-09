FROM tomcat:jdk21-temurin

MAINTAINER rymcu.com

RUN mkdir -p /logs/mortise

RUN rm -rf /usr/local/tomcat/webapps.dist

RUN rm -rf /usr/local/tomcat/webapps/*

COPY target/mortise.war /usr/local/tomcat/webapps/

EXPOSE 8080
