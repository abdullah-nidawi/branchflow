CREATE TABLE product (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES company(id),
    sku VARCHAR(64) NOT NULL,
    name VARCHAR(150) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_product_company_sku UNIQUE (company_id, sku)
);

CREATE TABLE stock_item (
    id BIGSERIAL PRIMARY KEY,
    branch_id BIGINT NOT NULL REFERENCES branch(id),
    product_id BIGINT NOT NULL REFERENCES product(id),
    quantity INTEGER NOT NULL DEFAULT 0,
    minimum_quantity INTEGER NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_stock_quantity_nonnegative CHECK (quantity >= 0),
    CONSTRAINT ck_stock_minimum_nonnegative CHECK (minimum_quantity >= 0),
    CONSTRAINT uq_stock_branch_product UNIQUE (branch_id, product_id)
);

CREATE TABLE stock_movement (
    id BIGSERIAL PRIMARY KEY,
    stock_item_id BIGINT NOT NULL REFERENCES stock_item(id),
    movement_type VARCHAR(24) NOT NULL,
    quantity_change INTEGER NOT NULL,
    quantity_after INTEGER NOT NULL,
    reason VARCHAR(300) NOT NULL,
    recorded_at TIMESTAMPTZ NOT NULL,
    transfer_id UUID,
    CONSTRAINT ck_movement_nonzero CHECK (quantity_change <> 0)
);
CREATE INDEX idx_movement_item_recorded ON stock_movement(stock_item_id, recorded_at DESC, id DESC);

CREATE TABLE task (
    id BIGSERIAL PRIMARY KEY,
    branch_id BIGINT NOT NULL REFERENCES branch(id),
    title VARCHAR(160) NOT NULL,
    description TEXT,
    priority VARCHAR(16) NOT NULL,
    status VARCHAR(20) NOT NULL,
    due_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_task_branch_status ON task(branch_id, status);

CREATE TABLE employee (
    id BIGSERIAL PRIMARY KEY,
    branch_id BIGINT NOT NULL REFERENCES branch(id),
    name VARCHAR(120) NOT NULL,
    email VARCHAR(254) NOT NULL,
    CONSTRAINT uq_employee_branch_email UNIQUE (branch_id, email)
);

CREATE TABLE shift (
    id BIGSERIAL PRIMARY KEY,
    branch_id BIGINT NOT NULL REFERENCES branch(id),
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    start_time TIMESTAMPTZ NOT NULL,
    end_time TIMESTAMPTZ NOT NULL,
    status VARCHAR(16) NOT NULL,
    CONSTRAINT ck_shift_end_after_start CHECK (end_time > start_time)
);
CREATE INDEX idx_shift_employee_time ON shift(employee_id, start_time, end_time);
