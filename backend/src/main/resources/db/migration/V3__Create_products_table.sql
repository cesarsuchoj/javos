CREATE TABLE products (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code TEXT UNIQUE,
    name TEXT NOT NULL,
    description TEXT,
    type TEXT NOT NULL,
    price REAL NOT NULL,
    cost REAL,
    stock_qty INTEGER DEFAULT 0,
    unit TEXT,
    active BOOLEAN NOT NULL DEFAULT 1,
    created_at TEXT,
    updated_at TEXT
);
