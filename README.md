# Vaultix API

API REST do **Vaultix — Seguro de Identidade Digital**, projeto FIAP 2025/2026 alinhado ao Tema 4 ESG: **Governança e Compliance**.

A aplicação expõe endpoints para gerenciar planos, clientes, assinaturas e incidentes de identidade digital, mantendo uma trilha de auditoria automática (LGPD Art. 16 e Art. 48) através de triggers PL/SQL no Oracle.

---

## Stack

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 17 |
| Framework | Spring Boot 3.3.5 |
| Persistência | Spring Data JPA + Hibernate |
| Banco | Oracle 19c (FIAP) |
| Migrations | Flyway 10 |
| Segurança | Spring Security 6 + JWT (jjwt 0.12) |
| Validação | Bean Validation (Jakarta) |
| Documentação | OpenAPI / Swagger UI (springdoc 2.6) |
| Build | Maven |
| Container | Docker (multi-stage) |

---

## Estrutura do projeto

```
vaultix-api/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── cleanup.sql                              ← script opcional pra resetar o schema
├── README.md
└── src/main/
    ├── java/br/com/fiap/vaultix/
    │   ├── VaultixApplication.java
    │   ├── config/         (SecurityConfig, OpenApiConfig)
    │   ├── controller/     (Auth, Cliente, Plano, Assinatura, Incidente, Auditoria)
    │   ├── domain/         (entidades JPA + enums)
    │   ├── dto/            (records de request/response com Bean Validation)
    │   ├── exception/      (ResourceNotFound, Business, GlobalExceptionHandler)
    │   ├── repository/     (Spring Data repositories)
    │   ├── security/       (JwtService, JwtAuthFilter, UserDetailsServiceImpl)
    │   └── service/        (regras de negócio)
    └── resources/
        ├── application.yml
        └── db/migration/
            ├── V1__create_tables.sql        ← schema (5 tabelas)
            ├── V2__create_triggers.sql      ← 4 triggers PL/SQL
            ├── V3__seed_data.sql            ← dados de teste
            └── V4__create_usuario_table.sql ← usuário admin para JWT
```

---

## Endpoints

A API expõe **6 grupos** de endpoints (muito além do mínimo de 4 exigido):

### Autenticação (público)
| Método | Rota | Descrição |
|---|---|---|
| POST | `/auth/register` | Cadastra usuário e retorna JWT |
| POST | `/auth/login` | Login → retorna JWT |

### Clientes
| Método | Rota | Descrição |
|---|---|---|
| GET | `/clientes` | Lista paginada |
| GET | `/clientes/{id}` | Busca por ID |
| POST | `/clientes` | Cria cliente |
| PUT | `/clientes/{id}` | Atualiza cliente |
| DELETE | `/clientes/{id}` | Remove (requer `ROLE_ADMIN`) |

### Planos
| Método | Rota | Descrição |
|---|---|---|
| GET | `/planos` | Lista paginada |
| GET | `/planos/{id}` | Busca por ID |
| POST | `/planos` | Cria plano |
| PUT | `/planos/{id}` | Atualiza plano |
| DELETE | `/planos/{id}` | Remove (requer `ROLE_ADMIN`) |

### Assinaturas
| Método | Rota | Descrição |
|---|---|---|
| GET | `/assinaturas` | Lista paginada |
| GET | `/assinaturas/{id}` | Busca por ID |
| GET | `/assinaturas/cliente/{idCliente}` | Assinaturas de um cliente |
| POST | `/assinaturas` | Cria assinatura |
| PUT | `/assinaturas/{id}` | Atualiza — `ATIVA → VENCIDA` dispara trigger de auditoria |
| DELETE | `/assinaturas/{id}` | Remove (requer `ROLE_ADMIN`) |

### Incidentes
| Método | Rota | Descrição |
|---|---|---|
| GET | `/incidentes` | Lista paginada |
| GET | `/incidentes/{id}` | Busca por ID |
| GET | `/incidentes/cliente/{idCliente}` | Histórico do cliente |
| POST | `/incidentes` | Registra — severidade `CRITICA` dispara trigger e escalona pra `EM_ATENDIMENTO` |
| PUT | `/incidentes/{id}` | Atualiza incidente completo |
| PATCH | `/incidentes/{id}/status` | Atualiza só o status — `→ RESOLVIDO` dispara trigger |
| DELETE | `/incidentes/{id}` | Remove (requer `ROLE_ADMIN`) |

### Auditoria (somente leitura — registros gerados via trigger)
| Método | Rota | Descrição |
|---|---|---|
| GET | `/auditoria` | Lista paginada |
| GET | `/auditoria/{id}` | Busca por ID |
| GET | `/auditoria/cliente/{idCliente}` | Trilha de um cliente |
| GET | `/auditoria/incidente/{idIncidente}` | Registros de um incidente |

### Swagger e healthcheck (público)
- `http://localhost:8080/swagger-ui.html` — documentação interativa
- `http://localhost:8080/v3/api-docs` — spec OpenAPI 3
- `http://localhost:8080/actuator/health` — health da aplicação

---

## Como rodar

### Pré-requisitos
- Acesso ao Oracle da FIAP (`oracle.fiap.com.br:1521/ORCL`)
- **OU** Java 17 + Maven 3.9+ **OU** Docker

### Credenciais já configuradas
```
DB user:     seu_usuario
DB password: sua_senha
admin API:   admin / admin123   (criado automaticamente pela migration V4)
```

### Passo 1 — Limpar o schema (se necessário)
Se o seu schema FIAP já tem as tabelas da atividade anterior (que é o caso), rode primeiro o `cleanup.sql` no SQL Developer / DBeaver:

1. Conecte com `seu_usuario / sua_senha` em `oracle.fiap.com.br:1521/ORCL`
2. Abra `cleanup.sql`, selecione tudo (Ctrl+A) e execute como script (F5)
3. Confirme que as tabelas sumiram

> **Por que isso é necessário?** O Flyway V1 cria as tabelas do zero. Se elas já existirem, o `CREATE TABLE` falha. O `baseline-on-migrate=true` ajuda em parte, mas a forma mais limpa é começar com o schema vazio.

### Passo 2A — Rodar com Docker (recomendado)
```bash
# build e sobe
docker compose up --build

# logs
docker compose logs -f vaultix-api

# derrubar
docker compose down
```

### Passo 2B — Rodar com Maven local
```bash
# build (gera o JAR)
mvn clean package -DskipTests

# rodar
mvn spring-boot:run

# OU rodar o JAR direto
java -jar target/vaultix-api.jar
```

Em ambos os casos, no primeiro startup o Flyway vai executar `V1 → V2 → V3 → V4` e criar todo o schema + dados + triggers + usuário admin.

### Passo 3 — Testar
```bash
# Login com admin
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Pega o "token" da resposta e usa nos próximos requests
curl http://localhost:8080/clientes \
  -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

Ou abra o Swagger UI direto: <http://localhost:8080/swagger-ui.html>

### Passo 4 — Importar a collection do Postman
1. Abra o Postman (ou Insomnia, que importa Postman v2.1)
2. Import → arraste o arquivo `vaultix-postman-collection.json`
3. Na collection, vá em **Variables** e confirme `base_url = http://localhost:8080`
4. Rode primeiro `Auth → Login Admin` — o token é salvo automaticamente em `{{token}}` via script de teste
5. Os outros endpoints já vão com auth Bearer configurada

---

## Recursos implementados (checklist da atividade)

- [x] **Mínimo 4 endpoints REST** — entregamos **23 endpoints** distribuídos em 6 controllers
- [x] **Spring Boot** — versão 3.3.5 (Web, Data JPA, Security, Validation, Actuator)
- [x] **Spring Security em endpoints pertinentes** — JWT obrigatório em tudo, exceto `/auth/**`, Swagger e healthcheck; `DELETE` exige `ROLE_ADMIN`
- [x] **Validação avançada** — Bean Validation em todos os DTOs (`@NotBlank`, `@Email`, `@Pattern`, `@Future`, `@DecimalMin`, etc.)
- [x] **Tratamento de exceções** — `@RestControllerAdvice` com 8 handlers retornando códigos HTTP coerentes (400, 401, 403, 404, 409, 422, 500)
- [x] **Banco Oracle** — driver `ojdbc11`, dialect `OracleDialect`
- [x] **Flyway** — 4 migrations versionadas (`V1` schema, `V2` triggers, `V3` seed, `V4` usuários)
- [x] **Docker** — `Dockerfile` multi-stage + `docker-compose.yml`
- [x] **OpenAPI/Swagger** — documentação interativa em `/swagger-ui.html`
- [x] **Triggers PL/SQL preservadas** — as 4 triggers da atividade anterior continuam disparando automaticamente

---

## Fluxo de demonstração das triggers

Para o avaliador conseguir ver as triggers do PL/SQL funcionando através da API:

1. **Trigger de incidente crítico** (`TRG_ESCALONA_STATUS_CRITICO` + `TRG_AUDIT_NOVO_INCIDENTE`):
   ```bash
   POST /incidentes
   {
     "idCliente": 1,
     "tipoIncidente": "SEQUESTRO_CONTA",
     "descricao": "Conta empresarial bloqueada por atacante",
     "severidade": "CRITICA"
   }
   ```
   Resultado: o incidente nasce com `status: EM_ATENDIMENTO` (e não `ABERTO`) e um registro aparece em `/auditoria` com `tipoAcao: ESCALONAMENTO_CRITICO`.

2. **Trigger de resolução** (`TRG_AUDIT_RESOLUCAO_INCIDENTE`):
   ```bash
   PATCH /incidentes/3/status
   { "status": "RESOLVIDO" }
   ```
   Resultado: registro automático em `/auditoria` com `tipoAcao: RESOLUCAO_INCIDENTE` e `resultado: CONCLUIDO`.

3. **Trigger de assinatura vencida** (`TRG_AUDIT_ASSINATURA_VENCIDA`):
   ```bash
   PUT /assinaturas/1
   {
     "idCliente": 1,
     "idPlano": 1,
     "dtVencimento": "2026-01-10",
     "status": "VENCIDA"
   }
   ```
   Resultado: registro em `/auditoria` mencionando o prazo de retenção da LGPD Art. 16.

---

## Variáveis de ambiente disponíveis

| Variável | Default | Descrição |
|---|---|---|
| `DB_URL` | `sua_url_do_banco` | URL JDBC do Oracle |
| `DB_USER` | `seu_usuario` | Usuário do banco |
| `DB_PASSWORD` | `sua_senha` | Senha do banco |
| `SERVER_PORT` | `8080` | Porta HTTP da aplicação |
| `JWT_SECRET` | (chave Base64 padrão) | Segredo para assinar JWTs |
| `JWT_EXPIRATION` | `86400` | Tempo de vida do token em segundos |

---

## Troubleshooting

| Erro | Causa provável | Solução |
|---|---|---|
| `ORA-00955: name is already used` | Schema já tem as tabelas | Rodar `cleanup.sql` |
| `Connection refused` / `IO Error` | Sem acesso ao Oracle FIAP | Conferir VPN/rede; o Oracle FIAP exige rede da FIAP |
| `Flyway validation failed: Migration checksum mismatch` | Migration foi editada após aplicada | `validate-on-migrate: false` já desabilita; senão, `cleanup.sql` zera tudo |
| `401 Unauthorized` | Sem JWT ou expirado | Refazer login em `/auth/login` |
| `403 Forbidden` no DELETE | Usuário sem `ROLE_ADMIN` | Logar como `admin / admin123` |

---

**FIAP 2025/2026** — Vaultix Squad
