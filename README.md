# Aluno Online - Spring Boot

Sistema academico em Spring Boot para gerenciar alunos, professores, disciplinas,
matriculas, boletim, lancamento de notas e relatorios interativos com PostgreSQL.

## Tecnologias

- Java 21 (versão usada na validação)
- Spring Boot 4
- Spring Data JPA / Hibernate
- PostgreSQL
- Maven Wrapper
- Lombok
- Jakarta Bean Validation
- Springdoc OpenAPI / Swagger UI
- Spring Security, BCrypt e JWT (Auth0)

## Funcionalidades

- CRUD de alunos e professores.
- CRUD de disciplinas com vinculo opcional a professor.
- CRUD de matriculas com status `MATRICULADO`, `APROVADO`, `REPROVADO` e `TRANCADO`.
- Trancamento de matricula e emissao de historico por aluno.
- Dashboard com KPIs academicos.
- Boletim por aluno usando view e function SQL.
- Lancamento de `nota1` e `nota2` pela API, com calculo da media e atualizacao do status.
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

## Apresentar com banco dedicado

Na pasta do projeto, execute:

```bash
bash scripts/apresentar.sh
```

Requer Java 21 e as ferramentas PostgreSQL `initdb`, `pg_ctl`, `createdb` e
`psql`. Configure `JAVA_HOME` para o Java 21 se necessario; nesta maquina o script
seleciona a instalacao em `/usr/lib/jvm/java-21-openjdk` automaticamente.

O script gera o JAR, inicia um banco exclusivo em `127.0.0.1:55434` e sobe a
aplicacao em `http://localhost:8081`. Mantenha o terminal aberto e aguarde a
mensagem de inicializacao do Spring. Depois abra o
[Swagger](http://localhost:8081/swagger-ui.html).

Os dados da apresentacao ficam em `.demo/`, uma pasta local ignorada pelo Git,
e sao preservados entre execucoes. O banco original nao e usado e o script nao
faz reset dos dados. **Ctrl+C** encerra a aplicacao e o banco iniciado pelo script.

O [roteiro de apresentacao com prints](docs/APRESENTACAO.md) mostra o cadastro,
login, JWT, botao **Authorize** e consulta protegida.

## Como Rodar com seu PostgreSQL

### 1. Requisitos

Instale:

- Java 21
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
export JWT_SECRET="troque-por-uma-chave-secreta-da-sua-aplicacao"
```

Valores padrao se voce nao informar variaveis:

```properties
DB_URL=jdbc:postgresql://localhost:5432/aluno-online
DB_USERNAME=postgres
DB_PASSWORD=postgres
```

### 4. Subir a Aplicacao

```bash
./mvnw spring-boot:run
```

Nesta máquina o Java padrão é 27. Para executar com a versão 21, usada nos testes:

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./mvnw spring-boot:run
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

Dados existentes de versoes anteriores exigem migracao para o modelo atual de
duas notas e novos status, incluindo as views SQL. O script de apresentacao usa
uma base propria, sem resetar ou migrar o banco original.

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
| `vw_matricula_detalhada` | Matricula com aluno, disciplina, professor, duas notas, media, status e situacao |
| `vw_aprovacao_disciplina` | Aprovacao, reprovacao, media e percentual por disciplina |
| `vw_carga_professor` | Disciplinas, carga horaria, matriculas e media por professor |

### Procedures e Function

| Objeto | Descricao |
| :--- | :--- |
| `sp_transferir_matricula(matricula_id, disciplina_nova_id)` | Transfere uma matricula para outra disciplina |
| `sp_lancar_nota(matricula_id, nota)` | Define as duas notas com o mesmo valor e marca `APROVADO` quando `nota >= 7`; nao possui endpoint proprio |
| `fn_media_aluno(aluno_id)` | Retorna a media geral do aluno |

## Endpoints

### Cadastro e Login

| Metodo | Endpoint | Descricao |
| :--- | :--- | :--- |
| `POST` | `/usuarios` | Cadastra login e senha; resposta 201 com ID e login |
| `POST` | `/login` | Autentica login e senha; resposta 200 com JWT |

Os endpoints academicos abaixo exigem o JWT no cabecalho
`Authorization: Bearer <token>`.

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
| `POST` | `/matriculas` | Cria matricula com status `MATRICULADO` |
| `GET` | `/matriculas` | Lista matriculas |
| `GET` | `/matriculas/{id}` | Busca por ID |
| `GET` | `/matriculas/aluno/{alunoId}` | Lista por aluno |
| `GET` | `/matriculas/disciplina/{disciplinaId}` | Lista por disciplina |
| `PUT` | `/matriculas/{id}` | Atualiza status |
| `DELETE` | `/matriculas/{id}` | Remove matricula |
| `POST` | `/matriculas/{id}/transferir` | Chama `sp_transferir_matricula` |
| `PATCH` | `/matriculas/trancar/{id}` | Tranca uma matricula com status `MATRICULADO` |
| `PATCH` | `/matriculas/atualizar-notas/{id}` | Atualiza `nota1` e `nota2`; calcula a media e define `APROVADO` ou `REPROVADO` quando ambas estao presentes |
| `GET` | `/matriculas/emitir-historico/{alunoId}` | Retorna dados do aluno, disciplinas, notas, medias e status |

### Dashboard e Relatorios

| Metodo | Endpoint | Descricao |
| :--- | :--- | :--- |
| `GET` | `/dashboard/kpis` | KPIs, rankings e agregados |
| `GET` | `/dashboard/boletim/{alunoId}` | Boletim completo do aluno |
| `GET` | `/dashboard/matriculas-ativas` | Matriculas para lancamento de nota |
| `GET` | `/dashboard/aluno/{alunoId}/media` | Chama `fn_media_aluno` e retorna a media geral |
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

Testes completos com PostgreSQL temporario, sem usar seu banco original
(requer `initdb`, `pg_ctl`, `createdb` e Java 21 configurado):

```bash
./scripts/test-security.sh
```

O script executa `./mvnw verify`, gera o JAR e remove o banco temporario ao terminar.
Se a porta 55433 estiver ocupada, informe `SECURITY_TEST_DB_PORT=55434`.

O roteiro detalhado da alteracao e da apresentacao esta em
[docs/SPRING_SECURITY_ETAPAS.md](docs/SPRING_SECURITY_ETAPAS.md).

Build sem executar testes:

```bash
./mvnw -DskipTests package
```

Checar endpoints principais:

```bash
curl -H "Authorization: Bearer $TOKEN" http://localhost:8081/alunos
curl -H "Authorization: Bearer $TOKEN" http://localhost:8081/dashboard/kpis
curl -H "Authorization: Bearer $TOKEN" http://localhost:8081/relatorios/metadata
```

Exemplo de consulta de relatorio:

```bash
curl -X POST http://localhost:8081/relatorios \
  -H "Authorization: Bearer $TOKEN" \
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

Cadastre um usuario em `POST /usuarios` e faca login em `POST /login`, enviando
`{"login":"seu-login","senha":"sua-senha"}`. O login devolve `{"token":"..."}`.
No Swagger, clique em **Authorize** e cole somente o valor do token, sem aspas
e sem o prefixo `Bearer`. Depois execute `GET /alunos`.

Na mini-aplicacao, a tela de acesso permite cadastrar e entrar. O token fica no
`sessionStorage` da aba e acompanha as requisicoes da API. O link **Sair** remove
esse token. A API e stateless; os JWTs expiram em duas horas.

O esquema `bearer-key` em `OpenApiConfig` e a anotacao
`@SecurityRequirement(name = "bearer-key")` nos controllers produzem o cadeado
do Swagger. `SecurityConfigurations` e `SecurityFilter` realizam a protecao real.

- Nao suba `.env`, senha real, token, chave privada ou arquivo local de IDE.
- `application.properties` usa `DB_URL`, `DB_USERNAME` e `DB_PASSWORD`.
- Configure `JWT_SECRET`; o valor padrao `12345678` reproduz somente o exemplo didatico da aula.
- O construtor de relatorios usa whitelist de views, colunas e operadores.
- Valores dos filtros entram por parametros JDBC, nao por concatenacao livre.
