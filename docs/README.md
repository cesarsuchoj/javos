# Javos - Documentação

> Sistema de gestão open-source, inspirado no projeto MAP-OS, implementado em Java com frontend moderno.

---

## 📋 Índice

1. [Objetivo do Projeto](#objetivo-do-projeto)
2. [Tecnologias](#tecnologias)
3. [Como Rodar Localmente](#como-rodar-localmente)
4. [Como Trocar de SQLite para MySQL](#como-trocar-de-sqlite-para-mysql)
5. [Estrutura de Pastas](#estrutura-de-pastas)
6. [Convenções](#convenções)
7. [Guias por Sistema Operacional](#guias-por-sistema-operacional)
8. [Licença](#licença)

---

## 🎯 Objetivo do Projeto

O **Javos** é um sistema de gestão desenvolvido em Java, inspirado no projeto open-source [MAP-OS](https://github.com/RamonSilva20/mapos) (originalmente escrito em PHP). Seu objetivo é oferecer uma alternativa moderna, robusta e escalável para gerenciamento de ordens de serviço, clientes, funcionários e demais recursos do MAP-OS — mas com a solidez do ecossistema Java e um frontend moderno em React.

O projeto é **100% open-source** e licenciado sob a **GNU General Public License v3.0**.

---

## 🛠️ Tecnologias

### Backend
- **Java 17** + **Spring Boot 3.x**
- **Spring Security** + **JWT** para autenticação
- **Spring Data JPA** + **Hibernate** para persistência
- **SQLite** (padrão) / **MySQL** (opcional)
- **Maven** para gerenciamento de dependências

### Frontend
- **React 18** + **TypeScript**
- **Vite** como bundler
- **React Router** para navegação
- **Zustand** para gerenciamento de estado
- **Axios** para chamadas HTTP

### Deploy
- **Docker** + **Docker Compose**
- Backend empacotado como JAR executável com Tomcat embutido
- Frontend compilado e servido pelo Spring Boot (arquivos estáticos)

---

## 🚀 Como Rodar Localmente

### Pré-requisitos
- Java 17+
- Maven 3.8+
- Node.js 18+ e npm
- Docker e Docker Compose (opcional, para deploy)

---

### Opção 1: Executar com Docker Compose (Recomendado)

```bash
# Na raiz do projeto
cd deploy
docker-compose up --build
```

Acesse: [http://localhost:8080](http://localhost:8080)

---

### Opção 2: Executar Manualmente

#### 1. Backend

```bash
cd backend
mvn spring-boot:run
```

O backend estará disponível em: `http://localhost:8080`

#### 2. Frontend (desenvolvimento)

```bash
cd frontend
npm install
npm run dev
```

O frontend estará disponível em: `http://localhost:3000`

#### 3. Build completo (frontend + backend integrados)

```bash
cd deploy
./build.sh
```

Isso irá:
1. Instalar dependências e compilar o frontend
2. Copiar o build estático para `backend/src/main/resources/static`
3. Compilar e empacotar o backend como JAR executável

Execute o JAR gerado:

```bash
java -jar backend/target/javos-0.0.1-SNAPSHOT.jar
```

---

### Usuário padrão

Ao iniciar pela primeira vez, o sistema cria automaticamente um usuário administrador:

| Campo    | Valor      |
|----------|------------|
| Usuário  | `admin`    |
| Senha    | `admin123` |

> ⚠️ **IMPORTANTE**: Altere a senha do administrador imediatamente em produção!

---

## 🔄 Como Trocar de SQLite para MySQL

### 1. Edite o arquivo `backend/src/main/resources/application.yml`

Comente o bloco SQLite e descomente o bloco MySQL:

```yaml
# Comente ou remova o bloco SQLite:
# spring:
#   datasource:
#     url: jdbc:sqlite:./data/javos.db
#     driver-class-name: org.sqlite.JDBC
#   jpa:
#     database-platform: org.hibernate.community.dialect.SQLiteDialect

# Descomente e configure o bloco MySQL:
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/javos?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: SEU_USUARIO
    password: SUA_SENHA
  jpa:
    database-platform: org.hibernate.dialect.MySQLDialect
    hibernate:
      ddl-auto: update
```

### 2. Crie o banco de dados no MySQL

```sql
CREATE DATABASE javos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'javos_user'@'localhost' IDENTIFIED BY 'javos_password';
GRANT ALL PRIVILEGES ON javos.* TO 'javos_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Reinicie a aplicação

O Hibernate irá criar/atualizar as tabelas automaticamente via `ddl-auto: update`.

---

## 📂 Estrutura de Pastas

```
javos/
├── backend/                         # Spring Boot (Java)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/javos/
│   │   │   │   ├── JavasApplication.java    # Entry point
│   │   │   │   ├── config/                  # Configurações (Security, JWT)
│   │   │   │   ├── controller/              # Controllers REST
│   │   │   │   ├── dto/                     # Data Transfer Objects
│   │   │   │   ├── exception/               # Tratamento de erros
│   │   │   │   ├── model/                   # Entidades JPA
│   │   │   │   ├── repository/              # Interfaces Spring Data
│   │   │   │   └── service/                 # Lógica de negócio
│   │   │   └── resources/
│   │   │       ├── application.yml          # Configuração principal
│   │   │       └── static/                  # Build do frontend (gerado)
│   │   └── test/                            # Testes automatizados
│   └── pom.xml                              # Dependências Maven
│
├── frontend/                        # React + TypeScript
│   ├── src/
│   │   ├── components/              # Componentes React
│   │   │   ├── auth/                # Autenticação
│   │   │   ├── dashboard/           # Dashboard
│   │   │   └── layout/              # Header, Sidebar
│   │   ├── pages/                   # Páginas da aplicação
│   │   ├── services/                # Chamadas à API REST
│   │   ├── store/                   # Gerenciamento de estado (Zustand)
│   │   └── types/                   # Tipos TypeScript
│   ├── package.json
│   └── vite.config.ts
│
├── docs/                            # Documentação do projeto
│   └── README.md
│
├── deploy/                          # Scripts e configurações de deploy
│   ├── Dockerfile
│   ├── docker-compose.yml
│   └── build.sh
│
├── LICENSE                          # GNU GPL v3.0
└── README.md                        # Visão geral do projeto
```

---

## 📐 Convenções

### Backend (Java)
- **Nomenclatura**: CamelCase para classes, camelCase para métodos e variáveis
- **Pacotes**: `com.javos.<módulo>` (controller, service, repository, model, dto, config, exception)
- **REST**: Endpoints no padrão `/api/<recurso>` (ex: `/api/users`, `/api/auth/login`)
- **DTOs**: Nunca expor entidades JPA diretamente na API; usar DTOs
- **Tratamento de erros**: Centralizado via `GlobalExceptionHandler` (`@RestControllerAdvice`)
- **Segurança**: JWT stateless, sem sessões no servidor
- **Testes**: JUnit 5 + Mockito + Spring Boot Test

### Frontend (TypeScript/React)
- **Componentes**: PascalCase (ex: `LoginPage`, `Dashboard`)
- **Hooks/Stores**: prefixo `use` (ex: `useAuthStore`)
- **CSS**: CSS Modules (`.module.css`) para escopo local
- **API**: Centralizada em `src/services/`
- **Estado global**: Zustand em `src/store/`
- **Tipagem**: interfaces TypeScript em `src/types/index.ts`

### Git
- **Branches**: `feature/<nome>`, `fix/<nome>`, `docs/<nome>`
- **Commits**: mensagens em inglês, imperativo (ex: `Add user authentication`)

---

## 🖥️ Guias por Sistema Operacional

Para instruções detalhadas de instalação, operação e solução de problemas em cada plataforma:

| Sistema | Guia |
|---------|------|
| 🪟 Windows (10 e 11) | [docs/README-windows.md](README-windows.md) |
| 🍎 macOS (incluindo Apple Silicon M1/M2/M3) | [docs/README-macos.md](README-macos.md) |
| 🐧 Linux (Ubuntu, Debian, Fedora e outros) | [docs/README-linux.md](README-linux.md) |
| ❓ Perguntas Frequentes (FAQ) | [docs/FAQ.md](FAQ.md) |
| ✅ Checklist de testes multiplataforma | [docs/CHECKLIST.md](CHECKLIST.md) |

---

## 📜 Licença

Este projeto é licenciado sob a **GNU General Public License v3.0**.

Você tem liberdade para usar, estudar, modificar e distribuir este software, desde que mantenha a mesma licença para trabalhos derivados.

Consulte o arquivo [LICENSE](../LICENSE) para detalhes completos.
