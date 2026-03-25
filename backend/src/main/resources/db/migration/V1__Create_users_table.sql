-- Javos - Migration V1
-- Criação da tabela de usuários

CREATE TABLE users (
    id         INTEGER      PRIMARY KEY AUTOINCREMENT,
    username   VARCHAR(50)  NOT NULL,
    email      VARCHAR(100) NOT NULL,
    password   VARCHAR(120) NOT NULL,
    name       VARCHAR(100) NOT NULL,
    role       VARCHAR(50)  NOT NULL,
    active     BOOLEAN      NOT NULL DEFAULT 1,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email    UNIQUE (email)
);
