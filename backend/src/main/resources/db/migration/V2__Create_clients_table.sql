CREATE TABLE clients (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    email TEXT UNIQUE,
    phone TEXT,
    document TEXT,
    address TEXT,
    city TEXT,
    state TEXT,
    zip_code TEXT,
    active BOOLEAN NOT NULL DEFAULT 1,
    notes TEXT,
    created_at TEXT,
    updated_at TEXT
);
