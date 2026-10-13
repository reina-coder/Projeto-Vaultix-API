<div align="center">

# Vaultix API

**Seguro de Identidade Digital** · Projeto FIAP 2025/2026

API REST em Java e Spring Boot, com banco Oracle, containerizada com Docker e entregue por um pipeline de CI/CD no GitHub Actions.

[Repositório](https://github.com/reina-coder/Projeto-Vaultix-API) · [Pipeline (Actions)](https://github.com/reina-coder/Projeto-Vaultix-API/actions)

</div>

---

## Índice

1. [Tecnologias utilizadas](#1-tecnologias-utilizadas)
2. [Como executar localmente com Docker](#2-como-executar-localmente-com-docker)
3. [Containerização](#3-containerização)
4. [Pipeline CI/CD](#4-pipeline-cicd)
5. [Prints do funcionamento](#5-prints-do-funcionamento)

---

## 1. Tecnologias utilizadas

| Categoria | Tecnologias |
|---|---|
| **Linguagem** | Java 17 |
| **Framework** | Spring Boot 3.3.5 (Web, Data JPA, Security, Validation, Actuator) |
| **Banco de dados** | Oracle (Oracle XE 21 em container) |
| **Migrations** | Flyway |
| **Segurança** | Spring Security + JWT (jjwt) |
| **Documentação da API** | OpenAPI / Swagger UI (springdoc) |
| **Testes** | JUnit 5, Mockito |
| **Build** | Maven |
| **Containerização** | Docker, Docker Compose |
| **CI/CD** | GitHub Actions, GitHub Container Registry, GitHub Environments |

---

## 2. Como executar localmente com Docker

### Pré-requisitos

- Docker com Docker Compose v2
- Portas **8080** (API) e **1521** (Oracle) livres

### Passo a passo

**Passo 1. Clonar o repositório**
```bash
git clone https://github.com/reina-coder/Projeto-Vaultix-API.git
cd Projeto-Vaultix-API
```

**Passo 2. Subir a aplicação (API + Oracle XE)**
```bash
docker compose up --build
```
> Na primeira execução o Oracle leva alguns minutos para iniciar. Depois, o Flyway cria automaticamente as tabelas, triggers, dados de teste e o usuário admin.

**Passo 3. Verificar se está no ar**
```bash
curl http://localhost:8080/actuator/health
```
Resposta esperada: `{"status":"UP"}`

**Passo 4. Fazer login**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```
A resposta traz um `token`, que deve ser enviado no cabeçalho `Authorization: Bearer <token>` nas demais requisições.

**Passo 5. Acessar a documentação interativa**

<http://localhost:8080/swagger-ui.html>

**Passo 6. Encerrar**
```bash
docker compose down        # derruba os containers
docker compose down -v     # derruba e apaga também os dados do banco
```

### Acessos rápidos

| Recurso | URL |
|---|---|
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Healthcheck | http://localhost:8080/actuator/health |
| Usuário de teste | `admin` / `admin123` |

---

## 3. Containerização

### 3.1 Dockerfile

```dockerfile
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
```

### 3.2 Estratégias adotadas

| Estratégia | Como foi aplicada | Benefício |
|---|---|---|
| **Multi-stage build** | Build (Maven + JDK) separado do runtime (apenas JRE) | Imagem final menor |
| **Cache de camadas** | `pom.xml` copiado e dependências baixadas antes do código-fonte | Builds seguintes mais rápidos |
| **Testes no build** | `mvn clean package` executa os testes dentro do build | A imagem só é gerada se os testes passarem |
| **Imagens Alpine** | Imagens base leves | Menor tamanho e superfície de ataque |
| **Usuário não-root** | Aplicação roda com o usuário `vaultix` | Mais segurança |
| **Healthcheck** | Consulta `/actuator/health` | O Docker sabe se a API está saudável |
| **Variáveis de ambiente** | Banco e JWT configurados por variáveis | A mesma imagem serve para local, staging e produção |

### 3.3 Docker Compose

O `docker-compose.yml` orquestra dois serviços:

| Serviço | Imagem | Função |
|---|---|---|
| `oracle` | `gvenzl/oracle-xe:21-slim` | Banco de dados local, com volume persistente |
| `vaultix-api` | Build do `Dockerfile` | API, que só inicia quando o Oracle está saudável |

---

## 4. Pipeline CI/CD

### 4.1 Visão geral

| Item | Descrição |
|---|---|
| **Ferramenta** | GitHub Actions |
| **Arquivo** | `.github/workflows/ci-cd.yml` |
| **Registro de imagens** | GitHub Container Registry (`ghcr.io/reina-coder/projeto-vaultix-api`) |
| **Ambientes** | `staging` e `production` (GitHub Environments) |
| **Pull Request para `main`** | Executa build e testes |
| **Push na `main`** | Executa o pipeline completo |

### 4.2 Fluxo

```
┌──────────────┐   ┌───────────────┐   ┌────────────────────┐   ┌────────────────┐   ┌──────────────────┐
│ Build e      │ → │ Imagem        │ → │ Teste de           │ → │ Deploy         │ → │ Deploy           │
│ testes       │   │ Docker        │   │ integração         │   │ Staging        │   │ Produção         │
└──────────────┘   └───────────────┘   └────────────────────┘   └────────────────┘   └──────────────────┘
                                                                    automático          aprovação manual
```

### 4.3 Etapas

| # | Etapa | O que faz |
|---|---|---|
| 1 | **Build e testes** | Executa `mvn clean verify`: compila, roda os testes automatizados e gera o JAR. Se algum teste falhar, o pipeline para. |
| 2 | **Imagem Docker** | Constrói a imagem pelo `Dockerfile` e publica no GHCR com as tags do commit e `latest`. |
| 3 | **Teste de integração** | Sobe a API e um Oracle XE com `docker compose`, aguarda o `/actuator/health` ficar `UP` e faz login com o admin. |
| 4 | **Deploy - Staging** | Automático. Envia o `deploy/docker-compose.deploy.yml` ao servidor por SSH, baixa a imagem, sobe o container e valida o healthcheck. |
| 5 | **Deploy - Produção** | Executa só se o staging passou e depende de aprovação manual. Implanta a mesma imagem validada em staging. |

### 4.4 Configuração do deploy

Os secrets abaixo são cadastrados em cada Environment (*Settings → Environments*):

| Secret | Descrição |
|---|---|
| `SSH_HOST`, `SSH_USER`, `SSH_KEY` | Acesso SSH ao servidor de destino |
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | Conexão com o Oracle do ambiente |
| `JWT_SECRET` | Segredo para assinar os tokens JWT |

> **Modo dry-run:** sem `SSH_HOST` e `SSH_KEY`, o deploy é apenas simulado (exibe ambiente, imagem e porta), mantendo o pipeline funcional. Com os secrets configurados, o deploy é real.

---

## 5. Prints do funcionamento

### 5.1 Execução do pipeline

Workflow **CI/CD Vaultix API** executado com sucesso na branch `main`, com os cinco jobs concluídos.

![Pipeline CI/CD executado com sucesso](docs/img/Captura%20de%20tela%202026-10-13%20194305.png)

### 5.2 Links de evidência

| Evidência | Link |
|---|---|
| Histórico de execuções | https://github.com/reina-coder/Projeto-Vaultix-API/actions |
| Imagem publicada | `ghcr.io/reina-coder/projeto-vaultix-api:latest` |

### 5.3 Staging e produção

| Ambiente | Evidência |
|---|---|
| **Staging** | *(inserir print do `/actuator/health`)* |
| **Produção** | *(inserir print do `/actuator/health`)* |