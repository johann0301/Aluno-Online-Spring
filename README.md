# Aluno Online - API Backend

Este projeto é um sistema de gerenciamento acadêmico desenvolvido em **Spring Boot**, focado no controle de alunos e professores. A API permite realizar operações de CRUD (Criar, Listar, Buscar, Atualizar e Deletar) para ambas as entidades, utilizando boas práticas de desenvolvimento Java e persistência de dados com PostgreSQL.

---

## 🛠️ Tecnologias Utilizadas

- **Java 21**
- **Spring Boot 4.0.3**
- **Spring Data JPA** (Persistência de dados)
- **PostgreSQL** (Banco de dados relacional)
- **Lombok** (Produtividade e redução de código boilerplate)
- **Jakarta Validation** (Validação de dados de entrada)
- **Maven** (Gerenciamento de dependências)

---

## 🚀 Como Executar o Projeto

1. **Configurar o Banco de Dados:**
   - Certifique-se de ter o PostgreSQL instalado.
   - Crie um banco de dados chamado `aluno-online`.
   - No arquivo `src/main/resources/application.properties`, ajuste o nome de usuário e a senha conforme sua configuração local:
     ```properties
     spring.datasource.username=seu_usuario
     spring.datasource.password=sua_senha
     ```

2. **Rodar a Aplicação:**
   - Execute o comando Maven no terminal:
     ```bash
     mvn spring-boot:run
     ```
   - A API estará disponível em `http://localhost:8081`.

---

## 📖 Documentação da API

### 👨‍🎓 Alunos (`/alunos`)

Gerenciamento de estudantes do sistema.

| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| `POST` | `/alunos` | Cria um novo aluno |
| `GET` | `/alunos` | Lista todos os alunos |
| `GET` | `/alunos/{id}` | Busca um aluno pelo ID |
| `PUT` | `/alunos/{id}` | Atualiza um aluno existente |
| `DELETE` | `/alunos/{id}` | Remove um aluno do sistema |

#### Exemplo de Body (POST/PUT):
```json
{
  "name": "Nome do Aluno",
  "email": "aluno@email.com",
  "cpf": "123.456.789-00"
}
```

---

### 👨‍🏫 Professores (`/professores`)

Gerenciamento de professores com validação de dados via DTO.

| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| `POST` | `/professores` | Cria um novo professor |
| `GET` | `/professores` | Lista todos os professores |
| `GET` | `/professores/{id}` | Busca um professor pelo ID |
| `PUT` | `/professores/{id}` | Atualiza um professor existente |
| `DELETE` | `/professores/{id}` | Remove um professor do sistema |

#### Exemplo de Body (POST/PUT):
> **Nota:** Todos os campos são obrigatórios e o e-mail deve ser válido.

```json
{
  "name": "Nome do Professor",
  "email": "professor@email.com",
  "cpf": "987.654.321-99"
}
```

---

## 📂 Estrutura do Projeto

- `controller`: Endpoints da API.
- `service`: Regras de negócio e lógica do sistema.
- `repository`: Interfaces de comunicação com o banco de dados.
- `model`: Entidades JPA que representam as tabelas.
- `dtos`: Objetos de transferência de dados com validações customizadas.
