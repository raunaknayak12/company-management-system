-- 02_dw_schema.sql: Star Schema Data Warehouse for CMS
CREATE SCHEMA IF NOT EXISTS dw;

-- Dimension: Date
CREATE TABLE IF NOT EXISTS dw.dim_date (
    date_key INT PRIMARY KEY,               -- YYYYMMDD
    full_date DATE NOT NULL UNIQUE,
    day_of_week VARCHAR(10) NOT NULL,
    day_of_month INT NOT NULL,
    month INT NOT NULL,
    month_name VARCHAR(15) NOT NULL,
    quarter INT NOT NULL,
    year INT NOT NULL,
    is_weekend BOOLEAN NOT NULL
);

-- Dimension: Customer
CREATE TABLE IF NOT EXISTS dw.dim_customer (
    customer_key SERIAL PRIMARY KEY,
    customer_id BIGINT,
    company_name VARCHAR(150) NOT NULL,
    region VARCHAR(50) NOT NULL,
    contact_name VARCHAR(100),
    email VARCHAR(100),
    created_at TIMESTAMP
);

-- Dimension: Product
CREATE TABLE IF NOT EXISTS dw.dim_product (
    product_key SERIAL PRIMARY KEY,
    product_id BIGINT,
    sku VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    category VARCHAR(100) NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    unit_cost NUMERIC(12, 2) NOT NULL
);

-- Dimension: Employee
CREATE TABLE IF NOT EXISTS dw.dim_employee (
    employee_key SERIAL PRIMARY KEY,
    employee_id BIGINT,
    employee_code VARCHAR(50) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    department_name VARCHAR(100) NOT NULL,
    job_title VARCHAR(100) NOT NULL
);

-- Fact: Sales Orders
CREATE TABLE IF NOT EXISTS dw.fact_sales_order (
    fact_id SERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    order_number VARCHAR(50) NOT NULL,
    date_key INT REFERENCES dw.dim_date(date_key),
    customer_key INT REFERENCES dw.dim_customer(customer_key),
    employee_key INT REFERENCES dw.dim_employee(employee_key),
    product_key INT REFERENCES dw.dim_product(product_key),
    region VARCHAR(50) NOT NULL,
    order_status VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    unit_cost NUMERIC(12, 2) NOT NULL,
    discount_percent NUMERIC(5, 2) DEFAULT 0.00,
    gross_amount NUMERIC(14, 2) NOT NULL,
    net_amount NUMERIC(14, 2) NOT NULL,
    cost_amount NUMERIC(14, 2) NOT NULL,
    profit_amount NUMERIC(14, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

-- Fact: Customer Feedback & Reviews
CREATE TABLE IF NOT EXISTS dw.fact_feedback (
    feedback_key SERIAL PRIMARY KEY,
    feedback_id BIGINT,
    customer_key INT REFERENCES dw.dim_customer(customer_key),
    product_key INT REFERENCES dw.dim_product(product_key),
    date_key INT REFERENCES dw.dim_date(date_key),
    rating INT NOT NULL,
    review_text TEXT,
    sentiment_label VARCHAR(20),       -- POSITIVE, NEUTRAL, NEGATIVE
    sentiment_score NUMERIC(5, 4),      -- Polarity (-1.0 to 1.0)
    source VARCHAR(50) DEFAULT 'CMS'   -- 'CMS' or 'REVIEWS_CSV'
);

-- ETL Run & Data Quality Log
CREATE TABLE IF NOT EXISTS dw.etl_runs_log (
    run_id SERIAL PRIMARY KEY,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    status VARCHAR(50) NOT NULL,       -- RUNNING, SUCCESS, FAILED
    rows_extracted_operational INT DEFAULT 0,
    rows_extracted_csv INT DEFAULT 0,
    rows_loaded_fact_sales INT DEFAULT 0,
    rows_loaded_fact_feedback INT DEFAULT 0,
    rows_rejected INT DEFAULT 0,
    error_message TEXT,
    quality_score NUMERIC(5, 2) DEFAULT 100.00
);

-- Indexes for lightning fast queries
CREATE INDEX IF NOT EXISTS idx_fact_sales_date ON dw.fact_sales_order(date_key);
CREATE INDEX IF NOT EXISTS idx_fact_sales_region ON dw.fact_sales_order(region);
CREATE INDEX IF NOT EXISTS idx_fact_sales_employee ON dw.fact_sales_order(employee_key);
CREATE INDEX IF NOT EXISTS idx_fact_sales_customer ON dw.fact_sales_order(customer_key);
CREATE INDEX IF NOT EXISTS idx_fact_sales_product ON dw.fact_sales_order(product_key);
CREATE INDEX IF NOT EXISTS idx_fact_feedback_date ON dw.fact_feedback(date_key);
