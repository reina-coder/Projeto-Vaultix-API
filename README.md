Vaultix API
API REST do Vaultix — Seguro de Identidade Digital (projeto FIAP 2025/2026), desenvolvida em Java com Spring Boot e Oracle.

Como executar localmente com Docker
Pré-requisitos: Docker com Docker Compose v2 e as portas 8080 (API) e 1521 (Oracle) livres.

Clone o repositório:
git clone https://github.com/reina-coder/Projeto-Vaultix-API.git
cd Projeto-Vaultix-API
Suba a aplicação (API + Oracle XE):
docker compose up --build
Na primeira execução o Oracle leva alguns minutos para iniciar. Depois, o Flyway cria as tabelas, triggers, dados de teste e o usuário admin.
Verifique se está no ar:
curl http://localhost:8080/actuator/health
Resposta esperada: {"status":"UP"}
Faça login (usuário admin, senha admin123) e use o token retornado:
curl -X POST http://localhost:8080/auth/login \
-H "Content-Type: application/json" \
-d '{"username":"admin","password":"admin123"}'
Acesse a documentação: http://localhost:8080/swagger-ui.html
Para encerrar: docker compose down (use -v para apagar também os dados do banco).
Pipeline CI/CD
Ferramenta: GitHub Actions (.github/workflows/ci-cd.yml). As imagens são publicadas no GitHub Container Registry (GHCR), e os ambientes staging e production são configurados em GitHub Environments.

Gatilhos: Pull Request para main executa build e testes. Push na main executa o pipeline completo.

Build e testes → Imagem Docker → Teste de integração → Deploy Staging → Deploy Produção
(aprovação manual)
Etapa	Funcionamento
Build e testes	Executa mvn clean verify: compila, roda os testes automatizados (JUnit/Mockito) e gera o JAR. Se algum teste falhar, o pipeline para.
Imagem Docker	Constrói a imagem pelo Dockerfile e publica no GHCR com as tags do commit e latest.
Teste de integração	Sobe a API e um Oracle XE com docker compose, espera o /actuator/health ficar UP e faz login com o admin.
Deploy - Staging	Automático. Envia o deploy/docker-compose.deploy.yml ao servidor por SSH, baixa a imagem, sobe o container e valida o healthcheck.
Deploy - Produção	Executa só se o staging passou e depende de aprovação manual. Implanta a mesma imagem validada em staging.
Configuração do deploy: os secrets SSH_HOST, SSH_USER, SSH_KEY, DB_URL, DB_USER, DB_PASSWORD e JWT_SECRET são cadastrados em cada Environment. Sem SSH_HOST e SSH_KEY, o deploy roda em modo dry-run (simulado), mantendo o pipeline funcional.

Containerização
Dockerfile
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
Estratégias adotadas
Multi-stage build: o build (Maven + JDK) fica separado do runtime (apenas JRE), gerando uma imagem final menor.
Cache de camadas: o pom.xml é copiado e as dependências baixadas antes do código-fonte, acelerando os builds seguintes.
Testes no build: mvn clean package executa os testes, então a imagem só é gerada se eles passarem.
Imagens Alpine: imagens leves e com menor superfície de ataque.
Usuário não-root: a aplicação roda com o usuário vaultix.
Healthcheck: o Docker consulta /actuator/health para saber se a API está saudável.
Configuração por variáveis de ambiente: a mesma imagem serve para local, staging e produção.
Docker Compose: docker-compose.yml orquestra a API e o Oracle XE, e a API só inicia quando o banco está saudável.
Prints do funcionamento
Execução do pipeline no GitHub Actions (build, testes, imagem, integração, staging e produção concluídos):

![Pipeline CI/CD executado com sucesso](docs/img/pipeline-sucesso.png)

Histórico de execuções: https://github.com/reina-coder/Projeto-Vaultix-API/actions

Imagem publicada: ghcr.io/reina-coder/projeto-vaultix-api:latest

Staging e produção: (inserir prints do /actuator/health de cada ambiente)

Tecnologias utilizadas
Categoria	Tecnologias
Linguagem	Java 17
Framework	Spring Boot 3.3.5 (Web, Data JPA, Security, Validation, Actuator)
Banco de dados	Oracle (Oracle XE 21 em container)
Migrations	Flyway
Segurança	Spring Security + JWT (jjwt)
Documentação	OpenAPI / Swagger UI (springdoc)
Testes	JUnit 5, Mockito
Build	Maven
Containerização	Docker, Docker Compose
CI/CD	GitHub Actions, GitHub Container Registry, GitHub Environments
