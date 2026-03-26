CREATE TABLE charges (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    client_id INTEGER,
    reference_id INTEGER,
    reference_type TEXT,
    amount REAL NOT NULL,
    due_date TEXT,
    status TEXT NOT NULL DEFAULT 'PENDING',
    method TEXT,
    external_id TEXT,
    notes TEXT,
    created_at TEXT,
    updated_at TEXT,
    FOREIGN KEY (client_id) REFERENCES clients(id)
);
