-- 03_queries.sql: Business Intelligence & Analytical Queries
-- Query 1: Monthly Revenue, Profit and Margin %
SELECT 
    d.year,
    d.month,
    d.month_name,
    COUNT(DISTINCT f.order_id) as total_orders,
    SUM(f.net_amount) as total_revenue,
    SUM(f.profit_amount) as total_profit,
    ROUND((SUM(f.profit_amount) / NULLIF(SUM(f.net_amount), 0) * 100), 2) as profit_margin_pct
FROM dw.fact_sales_order f
JOIN dw.dim_date d ON f.date_key = d.date_key
WHERE f.order_status IN ('CONFIRMED', 'PAID', 'DELIVERED')
GROUP BY d.year, d.month, d.month_name
ORDER BY d.year DESC, d.month DESC;

-- Query 2: Regional Performance Ranking
SELECT 
    f.region,
    COUNT(DISTINCT f.order_id) as total_orders,
    SUM(f.net_amount) as total_sales,
    SUM(f.profit_amount) as total_profit,
    ROUND(AVG(f.net_amount), 2) as avg_order_value
FROM dw.fact_sales_order f
WHERE f.order_status IN ('CONFIRMED', 'PAID', 'DELIVERED')
GROUP BY f.region
ORDER BY total_sales DESC;

-- Query 3: Top Performing Sales Representatives
SELECT 
    e.employee_code,
    e.full_name,
    e.department_name,
    COUNT(DISTINCT f.order_id) as orders_closed,
    SUM(f.net_amount) as total_sales,
    SUM(f.profit_amount) as total_profit
FROM dw.fact_sales_order f
JOIN dw.dim_employee e ON f.employee_key = e.employee_key
WHERE f.order_status IN ('CONFIRMED', 'PAID', 'DELIVERED')
GROUP BY e.employee_code, e.full_name, e.department_name
ORDER BY total_sales DESC;

-- Query 4: Customer RFM (Recency, Frequency, Monetary) Base
SELECT 
    c.customer_id,
    c.company_name,
    c.region,
    MAX(d.full_date) as last_order_date,
    CURRENT_DATE - MAX(d.full_date) as recency_days,
    COUNT(DISTINCT f.order_id) as frequency_orders,
    SUM(f.net_amount) as monetary_value
FROM dw.fact_sales_order f
JOIN dw.dim_customer c ON f.customer_key = c.customer_key
JOIN dw.dim_date d ON f.date_key = d.date_key
WHERE f.order_status IN ('CONFIRMED', 'PAID', 'DELIVERED')
GROUP BY c.customer_id, c.company_name, c.region;

-- Query 5: Product Category Margin & Volume Analysis
SELECT 
    p.category,
    COUNT(f.fact_id) as units_sold,
    SUM(f.net_amount) as total_revenue,
    SUM(f.profit_amount) as total_profit,
    ROUND((SUM(f.profit_amount) / NULLIF(SUM(f.net_amount), 0) * 100), 2) as margin_pct
FROM dw.fact_sales_order f
JOIN dw.dim_product p ON f.product_key = p.product_key
WHERE f.order_status IN ('CONFIRMED', 'PAID', 'DELIVERED')
GROUP BY p.category
ORDER BY total_revenue DESC;

-- Query 6: Sales Funnel Conversion Rates
SELECT 
    order_status,
    COUNT(*) as stage_count,
    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER (), 2) as pct_of_total
FROM dw.fact_sales_order
GROUP BY order_status
ORDER BY 
    CASE order_status
        WHEN 'INQUIRY' THEN 1
        WHEN 'QUOTED' THEN 2
        WHEN 'CONFIRMED' THEN 3
        WHEN 'PAID' THEN 4
        WHEN 'DELIVERED' THEN 5
        WHEN 'CANCELLED' THEN 6
        ELSE 7
    END;
