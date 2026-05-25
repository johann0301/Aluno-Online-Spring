# Aluno Online - Spring Boot

Sistema academico em Spring Boot para gerenciar alunos, professores, disciplinas,
matriculas, boletim, lancamento de notas e relatorios interativos com PostgreSQL.

## Tecnologias

- Java 21+
- Spring Boot 4
- Spring Data JPA / Hibernate
- PostgreSQL
- Maven Wrapper
- Lombok
- Jakarta Bean Validation
- Springdoc OpenAPI / Swagger UI

## Funcionalidades

- CRUD de alunos e professores.
- CRUD de disciplinas com vinculo opcional a professor.
- CRUD de matriculas com status `ATIVA`, `CONCLUIDA` e `TRANCADA`.
- Dashboard com KPIs academicos.
- Boletim por aluno usando view e function SQL.
- Lancamento de notas via procedure SQL.
- Transferencia de matricula entre disciplinas via procedure SQL.
- Relatorios interativos por view, campos, filtros, ordenacao e exportacao CSV.
- Seed de demonstracao com 10 alunos, 4 professores, 6 disciplinas e 28 matriculas.

## Estrutura Principal

```text
src/main/java/br/com/alunoonline/api/
├── config/      # Seed de dados demonstrativos
├── controller/  # Endpoints REST
├── dtos/        # Contratos de entrada/saida
├── model/       # Entidades JPA
├── repository/  # Repositorios Spring Data
└── service/     # Regras de negocio e acesso JDBC

src/main/resources/
├── db/          # Views, procedures e function PostgreSQL
└── static/      # Mini-aplicacao web
```

## Como Rodar

### 1. Requisitos

Instale:

- Java 21 ou superior
- PostgreSQL

Confira:

```bash
java -version
psql --version
```

### 2. Criar o Banco

Crie um banco chamado `aluno-online`:

```bash
createdb -h localhost -U postgres aluno-online
```

Se preferir pelo `psql`:

```sql
CREATE DATABASE "aluno-online";
```

### 3. Configurar Credenciais

O projeto nao versiona senha do banco. Configure por variaveis de ambiente:

```bash
export DB_URL="jdbc:postgresql://localhost:5432/aluno-online"
export DB_USERNAME="postgres"
export DB_PASSWORD="sua_senha_do_postgres"
```

Valores padrao se voce nao informar variaveis:

```properties
DB_URL=jdbc:postgresql://localhost:5432/aluno-online
DB_USERNAME=postgres
DB_PASSWORD=
```

### 4. Subir a Aplicacao

```bash
./mvnw spring-boot:run
```

Acesse:

```text
http://localhost:8081/
```

Documentacao Swagger:

```text
http://localhost:8081/swagger-ui.html
http://localhost:8081/v3/api-docs
```

### 5. Popular ou Resetar Dados de Demo

Por padrao, o seed so roda se o banco ainda nao tiver matriculas.

Para apagar os dados atuais e recriar o seed limpo, rode uma vez com:

```bash
APP_SEED_RESET=true ./mvnw spring-boot:run
```

Depois pare a aplicacao e suba normalmente:

```bash
./mvnw spring-boot:run
```

Tambem da para trocar temporariamente em `src/main/resources/application.properties`:

```properties
app.seed.reset=true
```

Depois do reset, volte para:

```properties
app.seed.reset=false
```

## Banco de Dados

O Spring executa automaticamente os scripts SQL ao iniciar:

```properties
spring.sql.init.mode=always
spring.sql.init.data-locations=classpath:db/01_views.sql,classpath:db/02_procedures.sql
spring.sql.init.separator=;;
spring.jpa.defer-datasource-initialization=true
```

Arquivos:

- `src/main/resources/db/01_views.sql`
- `src/main/resources/db/02_procedures.sql`

### Views

| View | Descricao |
| :--- | :--- |
| `vw_aluno_completo` | Aluno + contagem de matriculas por status |
| `vw_matricula_detalhada` | Matricula com aluno, disciplina, professor, nota, status e situacao |
| `vw_aprovacao_disciplina` | Aprovacao, reprovacao, media e percentual por disciplina |
| `vw_carga_professor` | Disciplinas, carga horaria, matriculas e media por professor |

### Procedures e Function

| Objeto | Descricao |
| :--- | :--- |
| `sp_transferir_matricula(matricula_id, disciplina_nova_id)` | Transfere uma matricula para outra disciplina |
| `sp_lancar_nota(matricula_id, nota)` | Lanca nota e conclui automaticamente quando `nota >= 7` |
| `fn_media_aluno(aluno_id)` | Retorna a media geral do aluno |

## Endpoints

### Alunos

| Metodo | Endpoint |
| :--- | :--- |
| `POST` | `/alunos` |
| `GET` | `/alunos` |
| `GET` | `/alunos/{id}` |
| `PUT` | `/alunos/{id}` |
| `DELETE` | `/alunos/{id}` |

### Professores

| Metodo | Endpoint |
| :--- | :--- |
| `POST` | `/professores` |
| `GET` | `/professores` |
| `GET` | `/professores/{id}` |
| `PUT` | `/professores/{id}` |
| `DELETE` | `/professores/{id}` |

### Disciplinas

| Metodo | Endpoint |
| :--- | :--- |
| `POST` | `/disciplina` |
| `GET` | `/disciplina` |
| `GET` | `/disciplina/{id}` |
| `PUT` | `/disciplina/{id}` |
| `DELETE` | `/disciplina/{id}` |

### Matriculas

| Metodo | Endpoint | Descricao |
| :--- | :--- | :--- |
| `POST` | `/matriculas` | Cria matricula |
| `GET` | `/matriculas` | Lista matriculas |
| `GET` | `/matriculas/{id}` | Busca por ID |
| `GET` | `/matriculas/aluno/{alunoId}` | Lista por aluno |
| `GET` | `/matriculas/disciplina/{disciplinaId}` | Lista por disciplina |
| `PUT` | `/matriculas/{id}` | Atualiza status |
| `DELETE` | `/matriculas/{id}` | Remove matricula |
| `POST` | `/matriculas/{id}/transferir` | Chama `sp_transferir_matricula` |
| `POST` | `/matriculas/{id}/nota` | Chama `sp_lancar_nota` |
| `GET` | `/matriculas/aluno/{alunoId}/media` | Chama `fn_media_aluno` |

### Dashboard e Relatorios

| Metodo | Endpoint | Descricao |
| :--- | :--- | :--- |
| `GET` | `/dashboard/kpis` | KPIs, rankings e agregados |
| `GET` | `/dashboard/boletim/{alunoId}` | Boletim completo do aluno |
| `GET` | `/dashboard/matriculas-ativas` | Matriculas para lancamento de nota |
| `GET` | `/relatorios/metadata` | Views e campos permitidos |
| `POST` | `/relatorios` | Consulta interativa em JSON |
| `POST` | `/relatorios/export.csv` | Exporta CSV |

### Swagger

| Metodo | Endpoint | Descricao |
| :--- | :--- | :--- |
| `GET` | `/swagger-ui.html` | Interface interativa da documentacao OpenAPI |
| `GET` | `/v3/api-docs` | Especificacao OpenAPI em JSON |

## Mini-Aplicacao Web

Com a API rodando, abra:

```text
http://localhost:8081/
```

Telas disponiveis:

- Dashboard
- Alunos
- Professores
- Disciplinas
- Matriculas
- Boletim
- Lancar Notas
- Relatorios

## Validacao

Build sem executar testes:

```bash
./mvnw -DskipTests package
```

Checar endpoints principais:

```bash
curl http://localhost:8081/alunos
curl http://localhost:8081/dashboard/kpis
curl http://localhost:8081/relatorios/metadata
```

Exemplo de consulta de relatorio:

```bash
curl -X POST http://localhost:8081/relatorios \
  -H "Content-Type: application/json" \
  -d '{
    "view": "vw_aluno_completo",
    "campos": ["aluno_id", "aluno_nome", "total_matriculas"],
    "filtros": [],
    "orderBy": "aluno_id",
    "orderDir": "ASC",
    "limit": 5
  }'
```

## Seguranca e Credenciais

- Nao suba `.env`, senha real, token, chave privada ou arquivo local de IDE.
- `application.properties` usa `DB_URL`, `DB_USERNAME` e `DB_PASSWORD`.
- O construtor de relatorios usa whitelist de views, colunas e operadores.
- Valores dos filtros entram por parametros JDBC, nao por concatenacao livre.
