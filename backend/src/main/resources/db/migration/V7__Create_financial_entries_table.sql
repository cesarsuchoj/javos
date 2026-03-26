CREATE TABLE financial_entries (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    description TEXT NOT NULL,
    type TEXT NOT NULL,
    amount REAL NOT NULL,
    due_date TEXT,
    payment_date TEXT,
    paid BOOLEAN NOT NULL DEFAULT 0,
    category_id INTEGER,
    account_id INTEGER,
    reference_id INTEGER,
    reference_type TEXT,
    notes TEXT,
    created_at TEXT,
    FOREIGN KEY (category_id) REFERENCES categories(id),
    FOREIGN KEY (account_id) REFERENCES accounts(id)
);
