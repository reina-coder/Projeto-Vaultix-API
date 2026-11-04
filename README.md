<div align="center">

# Vaultix API

**Seguro de Identidade Digital** · Projeto FIAP 2025/2026 · Tema ESG: **Governança e Compliance**

API REST em Java e Spring Boot para gerenciar clientes, planos, assinaturas e incidentes de segurança de identidade digital, com trilha de auditoria automática (LGPD), autenticação JWT, banco Oracle, testes BDD e pipeline de CI/CD.

[Repositório](https://github.com/reina-coder/Projeto-Vaultix-API) · [Pipeline (Actions)](https://github.com/reina-coder/Projeto-Vaultix-API/actions)

</div>

---

## Índice

1. [Sobre o projeto](#1-sobre-o-projeto)
2. [Funcionalidades](#2-funcionalidades)
3. [Tecnologias](#3-tecnologias)
4. [Arquitetura e estrutura de pastas](#4-arquitetura-e-estrutura-de-pastas)
5. [Modelo de dados e regras automáticas](#5-modelo-de-dados-e-regras-automáticas)
6. [Endpoints](#6-endpoints)
7. [Como executar](#7-como-executar)
8. [Configuração (variáveis de ambiente)](#8-configuração-variáveis-de-ambiente)
9. [Testes automatizados](#9-testes-automatizados)
10. [Containerização](#10-containerização)
11. [Pipeline CI/CD](#11-pipeline-cicd)
12. [Prints do funcionamento](#12-prints-do-funcionamento)

---

## 1. Sobre o projeto

O **Vaultix** é uma API para uma empresa de **seguro de identidade digital**. Ela registra os clientes, os planos comerciais e as assinaturas, e acompanha incidentes como phishing, invasão de conta, vazamento de dados e deepfake.

### Alinhamento com ESG (Governança e Compliance)

| Prática | Como aparece no projeto |
|---|---|
| **Rastreabilidade** | Toda abertura e resolução de incidente, e todo vencimento de assinatura, gera um registro na tabela de auditoria por *trigger* no banco. |
| **Integridade** | O schema é versionado com Flyway; restrições (`CHECK`, `UNIQUE`, chaves estrangeiras) garantem dados consistentes. |
| **Conformidade LGPD** | Trilha de auditoria somente leitura pela API; exclusões restritas ao perfil `ADMIN`. |
| **Segurança** | Autenticação JWT stateless, senhas com BCrypt, container com usuário não-root. |
| **Qualidade** | Testes unitários, testes BDD (Gherkin) e testes de contrato (JSON Schema) executados no pipeline. |

---

## 2. Funcionalidades

- Cadastro e login de usuários com **token JWT** (`/auth`).
- CRUD de **clientes**, com CPF (11 dígitos) e e-mail únicos.
- CRUD de **planos** (`BASICO`, `PREMIUM`, `ENTERPRISE`).
- CRUD de **assinaturas** ligando cliente e plano, com vigência e status.
- Registro e acompanhamento de **incidentes** de segurança, inclusive atualização só do status.
- Consulta da **auditoria** por registro, cliente ou incidente.
- Respostas de erro padronizadas (400, 401, 403, 404, 405, 409, 422, 500).
- Documentação interativa com **Swagger UI**.
- Healthcheck pelo **Actuator**.

---

## 3. Tecnologias

| Categoria | Tecnologias |
|---|---|
| **Linguagem** | Java 17 |
| **Framework** | Spring Boot 3.3.5 (Web, Data JPA, Security, Validation, Actuator) |
| **Banco de dados** | Oracle (Oracle XE 21 em container) |
| **Migrations** | Flyway |
| **Segurança** | Spring Security + JWT (jjwt 0.12) + BCrypt |
| **Documentação** | OpenAPI / Swagger UI (springdoc 2.6) |
| **Testes** | JUnit 5, Mockito, Cucumber (BDD/Gherkin), RestAssured, JSON Schema |
| **Build** | Maven |
| **Containerização** | Docker, Docker Compose |
| **CI/CD** | GitHub Actions, GitHub Container Registry, GitHub Environments |

---

## 4. Arquitetura e estrutura de pastas

Arquitetura em camadas: `controller` → `service` → `repository` → banco.

```
Projeto-Vaultix-API/
├── src/main/java/br/com/fiap/vaultix/
│   ├── config/         # Segurança (SecurityConfig) e OpenAPI
│   ├── controller/     # Endpoints REST
│   ├── service/        # Regras de negócio
│   ├── repository/     # Spring Data JPA
│   ├── domain/         # Entidades e enums
│   ├── dto/            # Requests/Responses (records) com validações
│   ├── security/       # JwtService, JwtAuthFilter, UserDetailsService
│   └── exception/      # GlobalExceptionHandler e exceções de negócio
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/   # V1 tabelas · V2 triggers · V3 dados de teste · V4 usuário admin
├── src/test/
│   ├── java/           # Testes unitários e runner/steps do Cucumber
│   └── resources/      # features (Gherkin) e schemas (JSON Schema)
├── deploy/             # docker-compose usado no deploy (staging/produção)
├── .github/            # Workflow de CI/CD e action de deploy
├── Dockerfile
├── docker-compose.yml  # API + Oracle XE para uso local
└── pom.xml
```

---

## 5. Modelo de dados e regras automáticas

### Tabelas

| Tabela | Descrição |
|---|---|
| `PLANO_ASSINATURA` | Catálogo de planos (nome, valor mensal, descrição, nível) |
| `CLIENTE` | Titulares do seguro (nome, e-mail único, CPF único, telefone) |
| `ASSINATURA` | Vínculo cliente-plano (início, vencimento, status) |
| `INCIDENTE` | Incidentes de segurança (tipo, severidade, status) |
| `AUDITORIA_SEGURANCA` | Trilha de auditoria gerada por triggers |
| `USUARIO` | Usuários da API (username, senha BCrypt, perfil `USER` ou `ADMIN`) |

### Valores permitidos

| Campo | Valores |
|---|---|
| Nível do plano | `BASICO`, `PREMIUM`, `ENTERPRISE` |
| Status da assinatura | `ATIVA`, `VENCIDA`, `CANCELADA`, `SUSPENSA` |
| Tipo de incidente | `HACKING_CONTA`, `PHISHING`, `ROUBO_DADOS`, `DEEPFAKE`, `SEQUESTRO_CONTA`, `VAZAMENTO_DADOS`, `OUTRO` |
| Severidade | `BAIXA`, `MEDIA`, `ALTA`, `CRITICA` |
| Status do incidente | `ABERTO`, `EM_ATENDIMENTO`, `AGUARDANDO_CLIENTE`, `RESOLVIDO`, `ENCERRADO` |

### Triggers (Oracle)

| Trigger | Quando dispara | O que faz |
|---|---|---|
| `TRG_AUDIT_NOVO_INCIDENTE` | Ao inserir incidente | Registra `ABERTURA_INCIDENTE` (ou `ESCALONAMENTO_CRITICO` se a severidade for `CRITICA`) |
| `TRG_ESCALONA_STATUS_CRITICO` | Antes de inserir incidente `CRITICA` | Força o status `EM_ATENDIMENTO` |
| `TRG_AUDIT_RESOLUCAO_INCIDENTE` | Status muda para `RESOLVIDO` | Registra `RESOLUCAO_INCIDENTE` |
| `TRG_AUDIT_ASSINATURA_VENCIDA` | Assinatura passa de `ATIVA` para `VENCIDA` | Registra `ASSINATURA_VENCIDA` |

### Regras de negócio

- Não é possível remover um **plano** que tenha assinaturas vinculadas (retorna `422`).
- Remover um **cliente** remove também suas assinaturas, incidentes e auditorias.
- Remover um **incidente** remove a trilha de auditoria dele.
- CPF e e-mail de clientes não podem se repetir (`422`).
- A data de vencimento de uma assinatura deve ser futura.

---

## 6. Endpoints

Todos os endpoints, exceto `/auth/**`, `/actuator/health`, `/actuator/info` e o Swagger, exigem o cabeçalho `Authorization: Bearer <token>`.
Operações `DELETE` exigem o perfil `ADMIN`. Listagens são paginadas (`?page=0&size=20&sort=id,asc`).

### Autenticação

| Método | Rota | Descrição | Sucesso |
|---|---|---|---|
| POST | `/auth/register` | Cadastra usuário (perfil `USER`) e retorna o token | 201 |
| POST | `/auth/login` | Autentica e retorna o token | 200 |

### Clientes, planos e assinaturas

| Método | Rota | Descrição |
|---|---|---|
| GET | `/clientes` · `/clientes/{id}` | Lista (paginada) · busca por ID |
| POST · PUT · DELETE | `/clientes` · `/clientes/{id}` | Cria · atualiza · remove (ADMIN) |
| GET | `/planos` · `/planos/{id}` | Lista (paginada) · busca por ID |
| POST · PUT · DELETE | `/planos` · `/planos/{id}` | Cria · atualiza · remove (ADMIN) |
| GET | `/assinaturas` · `/assinaturas/{id}` | Lista (paginada) · busca por ID |
| GET | `/assinaturas/cliente/{idCliente}` | Assinaturas de um cliente |
| POST · PUT · DELETE | `/assinaturas` · `/assinaturas/{id}` | Cria · atualiza · remove (ADMIN) |

### Incidentes e auditoria

| Método | Rota | Descrição |
|---|---|---|
| GET | `/incidentes` · `/incidentes/{id}` | Lista (paginada) · busca por ID |
| GET | `/incidentes/cliente/{idCliente}` | Histórico de um cliente |
| POST · PUT · DELETE | `/incidentes` · `/incidentes/{id}` | Cria · atualiza · remove (ADMIN) |
| PATCH | `/incidentes/{id}/status` | Atualiza apenas o status |
| GET | `/auditoria` · `/auditoria/{id}` | Lista (paginada) · busca por ID |
| GET | `/auditoria/cliente/{idCliente}` | Trilha de um cliente |
| GET | `/auditoria/incidente/{idIncidente}` | Trilha de um incidente |

### Códigos de resposta de erro

| Código | Quando acontece |
|---|---|
| 400 | Validação dos dados, JSON inválido ou valor de enum inexistente |
| 401 | Sem token, token inválido/expirado ou credenciais erradas |
| 403 | Usuário sem permissão (ex.: `USER` tentando `DELETE`) |
| 404 | Recurso ou rota inexistente |
| 405 | Verbo HTTP não suportado na rota |
| 409 | Violação de integridade no banco |
| 422 | Regra de negócio violada |

### Exemplos

**Login**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```
Resposta:
```json
{ "token": "eyJhbGciOi...", "tokenType": "Bearer", "expiresInSeconds": 86400 }
```

**Criar incidente**
```bash
curl -X POST http://localhost:8080/incidentes \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
        "idCliente": 1,
        "tipoIncidente": "PHISHING",
        "descricao": "Tentativa de phishing por e-mail falso de banco",
        "severidade": "MEDIA",
        "status": "ABERTO"
      }'
```

---

## 7. Como executar

### Pré-requisitos

- **Docker** com Docker Compose v2
- Portas **8080** (API) e **1521** (Oracle) livres
- *(Opcional, para rodar testes ou a API fora do Docker)* JDK 17 e Maven 3.9+

### Com Docker (recomendado)

```bash
git clone https://github.com/reina-coder/Projeto-Vaultix-API.git
cd Projeto-Vaultix-API
docker compose up --build
```

> Na primeira execução o Oracle leva alguns minutos para iniciar. Depois, o Flyway cria as tabelas, triggers, dados de teste e o usuário `admin`.

Verifique se está no ar:
```bash
curl http://localhost:8080/actuator/health     # {"status":"UP"}
```

Para encerrar:
```bash
docker compose down        # derruba os containers
docker compose down -v     # derruba e apaga também os dados do banco
```

### Acessos rápidos

| Recurso | URL |
|---|---|
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI (JSON) | http://localhost:8080/v3/api-docs |
| Healthcheck | http://localhost:8080/actuator/health |
| Usuário de teste | `admin` / `admin123` |

> Para testar no Swagger, faça login em `/auth/login`, copie o `token` e clique em **Authorize**.

### Sem Docker para a API

Suba apenas o Oracle (`docker compose up -d oracle`) e rode:
```bash
mvn spring-boot:run
```
Os valores padrão do `application.yml` já apontam para `localhost:1521/XEPDB1`.

---

## 8. Configuração (variáveis de ambiente)

Copie `.env.example` para `.env` e ajuste os valores. **Nunca versione o `.env`.**

| Variável | Para que serve | Padrão |
|---|---|---|
| `ORACLE_PASSWORD` | Senha do administrador do Oracle (container) | `oracle_password` |
| `DB_URL` | URL JDBC do banco | `jdbc:oracle:thin:@localhost:1521/XEPDB1` |
| `DB_USER` / `DB_PASSWORD` | Usuário e senha do banco | `vaultix` / `vaultix_password` |
| `JWT_SECRET` | Segredo JWT em **Base64**, mínimo 256 bits | valor de desenvolvimento |
| `JWT_EXPIRATION` | Validade do token em segundos | `86400` (24 h) |
| `SERVER_PORT` | Porta da API | `8080` |

> Em ambientes reais, gere um `JWT_SECRET` próprio, por exemplo com `openssl rand -base64 48`.

---

## 9. Testes automatizados

### 9.1 O que existe

| Tipo | Onde | O que valida |
|---|---|---|
| **Unitários** | `src/test/java/.../service` e `.../security` | Regras do `PlanoService` e do `JwtService`, com Mockito. Não precisam de banco nem de API. |
| **BDD (Gherkin)** | `src/test/resources/features/*.feature` | Cenários em português, com caminhos felizes e negativos: autenticação, clientes, planos, assinaturas, incidentes, auditoria e saúde da API. |
| **API (RestAssured)** | `src/test/java/.../bdd/steps/ApiSteps.java` | Chamadas reais: **status code**, **corpo JSON** e **contrato com JSON Schema**. |
| **Contratos** | `src/test/resources/schemas/*.json` | Um schema por tipo de resposta (objeto, página, lista e erro). |

Exemplo de cenário:

```gherkin
# language: pt
Cenário: Login com credenciais inválidas (caminho negativo)
  Quando eu envio uma requisição POST para "/auth/login" com o corpo:
    """
    { "username": "admin", "password": "senha_errada" }
    """
  Então o status code da resposta deve ser 401
  E o corpo da resposta não deve conter o campo "token"
```

### 9.2 Por que existem dois grupos de testes

Os testes BDD/API chamam a API **de verdade**, então precisam da aplicação e do Oracle no ar. Por isso ficam no profile Maven `api-tests`. O `mvn verify` comum (e o `docker build`) roda só os testes unitários e funciona em qualquer máquina.

### 9.3 Como executar

**Testes unitários**
```bash
mvn clean verify
```

**Testes BDD/API**
```bash
docker compose up -d --build                    # 1) sobe API + Oracle
curl http://localhost:8080/actuator/health      # 2) aguarde {"status":"UP"}
mvn test -Papi-tests                            # 3) executa os cenários
docker compose down -v                          # 4) encerra
```

Para apontar para outra URL: `mvn test -Papi-tests -Dapi.base.url=http://meu-host:8080`.

### 9.4 Relatórios

| Arquivo | Conteúdo |
|---|---|
| `target/cucumber-report.html` | Relatório visual dos cenários BDD |
| `target/cucumber-report.json` | Relatório em JSON |
| `target/surefire-reports/` | Relatórios do JUnit |

---

## 10. Containerização

- **Multi-stage build:** o Maven compila e testa em uma etapa; o runtime usa apenas o JRE.
- **Cache de camadas:** o `pom.xml` é copiado antes do código, para reaproveitar as dependências.
- **Imagens Alpine** e **usuário não-root** (`vaultix`).
- **Healthcheck** consultando `/actuator/health`.
- **Variáveis de ambiente** para banco e JWT: a mesma imagem serve para local, staging e produção.

O `docker-compose.yml` sobe dois serviços:

| Serviço | Imagem | Função |
|---|---|---|
| `oracle` | `gvenzl/oracle-xe:21-slim` | Banco local com volume persistente |
| `vaultix-api` | Build do `Dockerfile` | API, que só inicia quando o Oracle está saudável |

---

## 11. Pipeline CI/CD

Arquivo: `.github/workflows/ci-cd.yml` (GitHub Actions).

```
┌────────────┐   ┌──────────────┐   ┌─────────────────────┐   ┌──────────────┐   ┌────────────────┐
│ Build e    │ → │ Imagem       │ → │ Integração e        │ → │ Deploy       │ → │ Deploy         │
│ testes     │   │ Docker       │   │ BDD/API             │   │ Staging      │   │ Produção       │
└────────────┘   └──────────────┘   └─────────────────────┘   └──────────────┘   └────────────────┘
                                                                  automático      aprovação manual
```

| # | Etapa | O que faz |
|---|---|---|
| 1 | **Build e testes** | `mvn clean verify`: compila, roda os testes unitários e gera o JAR. |
| 2 | **Imagem Docker** | Publica a imagem no GHCR com as tags do commit e `latest` (somente na `main`). |
| 3 | **Integração e BDD/API** | Sobe API + Oracle com `docker compose`, aguarda o `/actuator/health`, faz login com o admin e executa os testes BDD/API. Relatórios no artefato `relatorios-bdd-api`. |
| 4 | **Deploy - Staging** | Automático: envia o compose por SSH, baixa a imagem, sobe o container e valida o healthcheck. |
| 5 | **Deploy - Produção** | Depende da aprovação manual configurada no Environment `production`. |

| Evento | O que executa |
|---|---|
| Pull Request para `main` | Build e testes + integração e BDD/API |
| Push na `main` | Pipeline completo |

### Secrets do deploy

Cadastrados em cada Environment (*Settings → Environments*):

| Secret | Descrição |
|---|---|
| `SSH_HOST`, `SSH_USER`, `SSH_KEY` | Acesso SSH ao servidor de destino |
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | Conexão com o Oracle do ambiente |
| `JWT_SECRET` | Segredo JWT em Base64 (mínimo 256 bits) |

> **Modo dry-run:** sem `SSH_HOST` e `SSH_KEY`, o deploy é apenas simulado (exibe ambiente, imagem e porta), mantendo o pipeline funcional.

---

## 12. Prints do funcionamento

### Execução do pipeline

![Pipeline CI/CD executado com sucesso](docs/img/Captura%20de%20tela%202026-10-13%20194305.png)

### Links de evidência

| Evidência | Link |
|---|---|
| Histórico de execuções | https://github.com/reina-coder/Projeto-Vaultix-API/actions |
| Imagem publicada | `ghcr.io/reina-coder/projeto-vaultix-api:latest` |

### Staging e produção

| Ambiente | Evidência |
|---|---|
| **Staging** | *(inserir print do `/actuator/health`)* |
| **Produção** | *(inserir print do `/actuator/health`)* |

---

<div align="center">

Projeto acadêmico · FIAP · Tema ESG: Governança e Compliance

</div>
