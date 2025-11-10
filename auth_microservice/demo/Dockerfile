# ---- BUILD STAGE ----
FROM maven:3.9-eclipse-temurin-17 AS build

ENV TZ=UTC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
ENV CATALINA_OPTS="-Duser.timezone=UTC"

WORKDIR /demo
COPY pom.xml .
COPY src ./src

# This will fetch dependencies as part of the build
RUN mvn -B clean install -DskipTests


# ---- RUNTIME STAGE ----
FROM eclipse-temurin:17-jdk
WORKDIR /demo
# Note the artifact name change to 'auth-microservice'
COPY --from=build /demo/target/auth-microservice-0.0.1-SNAPSHOT.jar /demo/demo.jar

# This container will run on port 8082
EXPOSE 8082
CMD ["java", "-jar", "demo.jar"]