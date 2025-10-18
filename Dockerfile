FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace/app

COPY mvnw mvnw.cmd ./
COPY .mvn/ .mvn
COPY pom.xml .

RUN ./mvnw -B dependency:go-offline

COPY src ./src

RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre AS runtime

LABEL org.opencontainers.image.source="https://github.com/example/taskify" \
      org.opencontainers.image.description="Taskify Spring Boot demo application" \
      org.opencontainers.image.licenses="MIT"

ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS=""

WORKDIR /opt/taskify

RUN groupadd --system taskify && useradd --system --gid taskify taskify

ARG JAR_FILE=target/*.jar
COPY --from=build /workspace/app/${JAR_FILE} app.jar

EXPOSE 8080

USER taskify

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
