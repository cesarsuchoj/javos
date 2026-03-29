-- Performance indexes: FK columns and frequently searched/filtered columns

-- clients
CREATE INDEX IF NOT EXISTS idx_clients_active     ON clients(active);
CREATE INDEX IF NOT EXISTS idx_clients_name       ON clients(name);
CREATE INDEX IF NOT EXISTS idx_clients_document   ON clients(document);

-- products
CREATE INDEX IF NOT EXISTS idx_products_active    ON products(active);
CREATE INDEX IF NOT EXISTS idx_products_type      ON products(type);
CREATE INDEX IF NOT EXISTS idx_products_name      ON products(name);

-- service_orders
CREATE INDEX IF NOT EXISTS idx_so_client_id       ON service_orders(client_id);
CREATE INDEX IF NOT EXISTS idx_so_technician_id   ON service_orders(technician_id);
CREATE INDEX IF NOT EXISTS idx_so_status          ON service_orders(status);

-- os_notes
CREATE INDEX IF NOT EXISTS idx_os_notes_so_id     ON os_notes(service_order_id);

-- os_attachments
CREATE INDEX IF NOT EXISTS idx_os_att_so_id       ON os_attachments(service_order_id);

-- sales
CREATE INDEX IF NOT EXISTS idx_sales_client_id    ON sales(client_id);
CREATE INDEX IF NOT EXISTS idx_sales_seller_id    ON sales(seller_id);
CREATE INDEX IF NOT EXISTS idx_sales_status       ON sales(status);

-- sale_items
CREATE INDEX IF NOT EXISTS idx_sale_items_sale_id    ON sale_items(sale_id);
CREATE INDEX IF NOT EXISTS idx_sale_items_product_id ON sale_items(product_id);

-- charges
CREATE INDEX IF NOT EXISTS idx_charges_client_id        ON charges(client_id);
CREATE INDEX IF NOT EXISTS idx_charges_status           ON charges(status);
CREATE INDEX IF NOT EXISTS idx_charges_ref              ON charges(reference_id, reference_type);

-- financial_entries
CREATE INDEX IF NOT EXISTS idx_fin_entries_category_id  ON financial_entries(category_id);
CREATE INDEX IF NOT EXISTS idx_fin_entries_account_id   ON financial_entries(account_id);
CREATE INDEX IF NOT EXISTS idx_fin_entries_type         ON financial_entries(type);
CREATE INDEX IF NOT EXISTS idx_fin_entries_paid         ON financial_entries(paid);

-- refresh_tokens
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id   ON refresh_tokens(user_id);

-- revoked_access_tokens
CREATE INDEX IF NOT EXISTS idx_revoked_expiry           ON revoked_access_tokens(expiry_date);

-- audit_log
CREATE INDEX IF NOT EXISTS idx_audit_username           ON audit_log(username);
CREATE INDEX IF NOT EXISTS idx_audit_entity             ON audit_log(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_created_at         ON audit_log(created_at);

-- email_queue
CREATE INDEX IF NOT EXISTS idx_email_queue_status       ON email_queue(status);
