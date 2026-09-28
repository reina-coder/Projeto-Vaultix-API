# --- Build stage ---
FROM maven:3.9.9-eclipse-temurin-17-alpine AS build
WORKDIR /build

# Copia o POM e baixa dependencias (cache de camada)
COPY pom.xml .
RUN mvn -B -e -ntp dependency:go-offline

# Copia o codigo e empacota
COPY src ./src
RUN mvn -B -e -ntp clean package -DskipTests

# --- Runtime stage ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Usuario nao-root para seguranca
RUN addgroup -S vaultix && adduser -S vaultix -G vaultix
USER vaultix

COPY --from=build /build/target/vaultix-api.jar app.jar

EXPOSE 8080

# Healthcheck via actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
