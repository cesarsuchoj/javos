-- CHECK constraints enforced via triggers (SQLite does not support ALTER TABLE ADD CONSTRAINT)

-- charges: amount must be positive
CREATE TRIGGER IF NOT EXISTS chk_charges_amount_insert
BEFORE INSERT ON charges
FOR EACH ROW
WHEN NEW.amount <= 0
BEGIN
    SELECT RAISE(ABORT, 'charge amount must be positive');
END;

CREATE TRIGGER IF NOT EXISTS chk_charges_amount_update
BEFORE UPDATE ON charges
FOR EACH ROW
WHEN NEW.amount <= 0
BEGIN
    SELECT RAISE(ABORT, 'charge amount must be positive');
END;

-- financial_entries: amount must be positive
CREATE TRIGGER IF NOT EXISTS chk_fin_entries_amount_insert
BEFORE INSERT ON financial_entries
FOR EACH ROW
WHEN NEW.amount <= 0
BEGIN
    SELECT RAISE(ABORT, 'financial entry amount must be positive');
END;

CREATE TRIGGER IF NOT EXISTS chk_fin_entries_amount_update
BEFORE UPDATE ON financial_entries
FOR EACH ROW
WHEN NEW.amount <= 0
BEGIN
    SELECT RAISE(ABORT, 'financial entry amount must be positive');
END;

-- service_orders: labor_cost must be non-negative
CREATE TRIGGER IF NOT EXISTS chk_so_labor_cost_insert
BEFORE INSERT ON service_orders
FOR EACH ROW
WHEN NEW.labor_cost < 0
BEGIN
    SELECT RAISE(ABORT, 'service order labor_cost cannot be negative');
END;

CREATE TRIGGER IF NOT EXISTS chk_so_labor_cost_update
BEFORE UPDATE ON service_orders
FOR EACH ROW
WHEN NEW.labor_cost < 0
BEGIN
    SELECT RAISE(ABORT, 'service order labor_cost cannot be negative');
END;

-- products: price must be non-negative
CREATE TRIGGER IF NOT EXISTS chk_products_price_insert
BEFORE INSERT ON products
FOR EACH ROW
WHEN NEW.price < 0
BEGIN
    SELECT RAISE(ABORT, 'product price cannot be negative');
END;

CREATE TRIGGER IF NOT EXISTS chk_products_price_update
BEFORE UPDATE ON products
FOR EACH ROW
WHEN NEW.price < 0
BEGIN
    SELECT RAISE(ABORT, 'product price cannot be negative');
END;

-- products: stock_qty must be non-negative
CREATE TRIGGER IF NOT EXISTS chk_products_stock_insert
BEFORE INSERT ON products
FOR EACH ROW
WHEN NEW.stock_qty < 0
BEGIN
    SELECT RAISE(ABORT, 'product stock_qty cannot be negative');
END;

CREATE TRIGGER IF NOT EXISTS chk_products_stock_update
BEFORE UPDATE ON products
FOR EACH ROW
WHEN NEW.stock_qty < 0
BEGIN
    SELECT RAISE(ABORT, 'product stock_qty cannot be negative');
END;

-- sale_items: quantity must be positive
CREATE TRIGGER IF NOT EXISTS chk_sale_items_qty_insert
BEFORE INSERT ON sale_items
FOR EACH ROW
WHEN NEW.quantity <= 0
BEGIN
    SELECT RAISE(ABORT, 'sale item quantity must be positive');
END;

CREATE TRIGGER IF NOT EXISTS chk_sale_items_qty_update
BEFORE UPDATE ON sale_items
FOR EACH ROW
WHEN NEW.quantity <= 0
BEGIN
    SELECT RAISE(ABORT, 'sale item quantity must be positive');
END;

-- sale_items: unit_price must be non-negative
CREATE TRIGGER IF NOT EXISTS chk_sale_items_price_insert
BEFORE INSERT ON sale_items
FOR EACH ROW
WHEN NEW.unit_price < 0
BEGIN
    SELECT RAISE(ABORT, 'sale item unit_price cannot be negative');
END;

CREATE TRIGGER IF NOT EXISTS chk_sale_items_price_update
BEFORE UPDATE ON sale_items
FOR EACH ROW
WHEN NEW.unit_price < 0
BEGIN
    SELECT RAISE(ABORT, 'sale item unit_price cannot be negative');
END;

-- sales: discount must be non-negative
CREATE TRIGGER IF NOT EXISTS chk_sales_discount_insert
BEFORE INSERT ON sales
FOR EACH ROW
WHEN NEW.discount < 0
BEGIN
    SELECT RAISE(ABORT, 'sale discount cannot be negative');
END;

CREATE TRIGGER IF NOT EXISTS chk_sales_discount_update
BEFORE UPDATE ON sales
FOR EACH ROW
WHEN NEW.discount < 0
BEGIN
    SELECT RAISE(ABORT, 'sale discount cannot be negative');
END;
