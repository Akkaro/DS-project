FROM maven:3.9 AS build

ENV TZ=UTC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
ENV CATALINA_OPTS="-Duser.timezone=UTC"

WORKDIR /demo

# Copy pom.xml and download dependencies
COPY ./pom.xml .
RUN mvn dependency:go-offline -q

# Copy source code
COPY ./src ./src

# Build the application
RUN mvn clean package -DskipTests -q && \
    ls -la /demo/target/ && \
    ls -la /demo/target/*.jar || echo "JAR not found!"

# Extract the JAR name dynamically
RUN JAR_FILE=$(ls /demo/target/*.jar | grep -v sources | head -1) && \
    echo "Found JAR: $JAR_FILE" && \
    cp "$JAR_FILE" /demo/app.jar

# Runtime stage
FROM openjdk:25-jdk

WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /demo/app.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]