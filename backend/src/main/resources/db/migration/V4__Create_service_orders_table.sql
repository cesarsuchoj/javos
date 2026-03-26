CREATE TABLE service_orders (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_number TEXT NOT NULL UNIQUE,
    client_id INTEGER NOT NULL,
    technician_id INTEGER,
    status TEXT NOT NULL DEFAULT 'OPEN',
    priority TEXT NOT NULL DEFAULT 'NORMAL',
    description TEXT NOT NULL,
    diagnosis TEXT,
    solution TEXT,
    labor_cost REAL DEFAULT 0,
    estimated_completion TEXT,
    completed_at TEXT,
    created_at TEXT,
    updated_at TEXT,
    FOREIGN KEY (client_id) REFERENCES clients(id),
    FOREIGN KEY (technician_id) REFERENCES users(id)
);
