# ---- BUILD STAGE ----
FROM maven:3.9-eclipse-temurin-17 AS build

ENV TZ=UTC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
ENV CATALINA_OPTS="-Duser.timezone=UTC"

WORKDIR /demo
COPY pom.xml .
COPY src ./src

RUN mvn -B clean install -DskipTests


# ---- RUNTIME STAGE ----
FROM eclipse-temurin:17-jdk
WORKDIR /demo
COPY --from=build /demo/target/device-microservice-0.0.1-SNAPSHOT.jar /demo/demo.jar

EXPOSE 8080
CMD ["java", "-jar", "demo.jar"]
