FROM openjdk:17-slim-buster

# Optional JVM arguments, such as memory settings
ARG JVM_ARGS=""

WORKDIR /app

COPY ./build/libs/app.jar /app

EXPOSE 8181

ENV WEB_HTTP_PORT="8181"
ENV WEB_HTTP_PATH="/api"

ENV JVM_ARGS=$JVM_ARGS
ENTRYPOINT [ "sh", "-c", "java $JVM_ARGS -jar app.jar"]
