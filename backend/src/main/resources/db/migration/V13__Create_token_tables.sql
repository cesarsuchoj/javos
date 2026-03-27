-- Javos - Migration V13
-- Tabelas para refresh tokens e revogação de access tokens

CREATE TABLE refresh_tokens (
    id          INTEGER      PRIMARY KEY AUTOINCREMENT,
    user_id     INTEGER      NOT NULL,
    token       VARCHAR(255) NOT NULL,
    expiry_date TEXT         NOT NULL,
    revoked     BOOLEAN      NOT NULL DEFAULT 0,
    created_at  TEXT         NOT NULL DEFAULT (datetime('now')),
    CONSTRAINT uq_refresh_tokens_token UNIQUE (token),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE revoked_access_tokens (
    id          INTEGER      PRIMARY KEY AUTOINCREMENT,
    jti         VARCHAR(36)  NOT NULL,
    username    VARCHAR(50)  NOT NULL,
    expiry_date TEXT         NOT NULL,
    revoked_at  TEXT         NOT NULL DEFAULT (datetime('now')),
    CONSTRAINT uq_revoked_tokens_jti UNIQUE (jti)
);
