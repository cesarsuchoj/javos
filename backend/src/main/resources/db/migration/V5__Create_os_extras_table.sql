CREATE TABLE os_attachments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    service_order_id INTEGER NOT NULL,
    file_name TEXT NOT NULL,
    file_url TEXT NOT NULL,
    file_type TEXT,
    created_at TEXT,
    FOREIGN KEY (service_order_id) REFERENCES service_orders(id)
);

CREATE TABLE os_notes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    service_order_id INTEGER NOT NULL,
    author_id INTEGER,
    content TEXT NOT NULL,
    created_at TEXT,
    FOREIGN KEY (service_order_id) REFERENCES service_orders(id),
    FOREIGN KEY (author_id) REFERENCES users(id)
);
