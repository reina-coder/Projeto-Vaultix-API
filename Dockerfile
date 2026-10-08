# =========================
# Etapa 1 - Build
# =========================
FROM maven:3.9.9-eclipse-temurin-17-alpine AS build

WORKDIR /build

# Copia primeiro o pom para aproveitar o cache do Docker
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline

# Copia o código da aplicação e os testes
COPY src ./src

# Compila e executa os testes automatizados
RUN mvn -B -ntp clean package

# =========================
# Etapa 2 - Runtime
# =========================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Executa a aplicação com usuário não-root
RUN addgroup -S vaultix && adduser -S vaultix -G vaultix
USER vaultix

COPY --from=build /build/target/vaultix-api.jar app.jar

EXPOSE 8080

# O Actuator será usado para verificar se a API está saudável
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
