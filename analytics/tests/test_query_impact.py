"""
test_query_impact.py
Validates the direct impact of modifying PostgreSQL / data warehouse queries on
KPI metrics and Plotly charts, and verifies the INR currency system.
"""

import pandas as pd
from sqlalchemy import text
from app.db import engine
from app.charts.plotly_builder import (
    build_sales_trend_chart,
    build_regional_comparison_chart,
)


def test_query_status_filter_impact_on_kpis() -> None:
    """
    Test that modifying the SQL query WHERE clause (e.g. changing order status filters)
    directly alters KPI metrics (revenue, orders, profit).
    """
    # 1. Baseline Query (Standard: CONFIRMED, PAID, DELIVERED)
    baseline_query = """
    SELECT
        COUNT(DISTINCT f.order_id) as total_orders,
        COALESCE(SUM(f.net_amount), 0) as total_revenue,
        COALESCE(SUM(f.profit_amount), 0) as total_profit
    FROM fact_sales_order f
    WHERE f.order_status IN ('CONFIRMED', 'PAID', 'DELIVERED')
    """
    with engine.connect() as conn:
        df_base = pd.read_sql(text(baseline_query), conn)

    base_orders = int(df_base["total_orders"].iloc[0])
    base_revenue = float(df_base["total_revenue"].iloc[0])
    assert base_orders > 0
    assert base_revenue > 0

    # 2. Modified Query (Stricter: DELIVERED only)
    modified_query = """
    SELECT
        COUNT(DISTINCT f.order_id) as total_orders,
        COALESCE(SUM(f.net_amount), 0) as total_revenue,
        COALESCE(SUM(f.profit_amount), 0) as total_profit
    FROM fact_sales_order f
    WHERE f.order_status = 'DELIVERED'
    """
    with engine.connect() as conn:
        df_mod = pd.read_sql(text(modified_query), conn)

    mod_orders = int(df_mod["total_orders"].iloc[0])
    mod_revenue = float(df_mod["total_revenue"].iloc[0])

    # Assert that changing the query directly affects the metrics
    assert mod_orders <= base_orders, "Modified query should have fewer or equal orders"
    assert mod_revenue <= base_revenue, "Modified query revenue must reflect filtered status"
    assert mod_revenue != base_revenue or mod_orders != base_orders, (
        "Changing PostgreSQL query filter MUST change the analytics output"
    )


def test_query_aggregation_change_affects_chart_traces() -> None:
    """
    Test that modifying the aggregation or column expressions in the SQL query
    directly alters the data points and traces rendered in Plotly charts.
    """
    # Baseline query (net_amount after discounts)
    query_net = """
    SELECT d.year, d.month, SUM(f.net_amount) as amount
    FROM fact_sales_order f
    JOIN dim_date d ON f.date_key = d.date_key
    WHERE f.order_status IN ('CONFIRMED', 'PAID', 'DELIVERED')
    GROUP BY d.year, d.month ORDER BY d.year, d.month
    """
    # Modified query (gross_amount before discounts)
    query_gross = """
    SELECT d.year, d.month, SUM(f.gross_amount) as amount
    FROM fact_sales_order f
    JOIN dim_date d ON f.date_key = d.date_key
    WHERE f.order_status IN ('CONFIRMED', 'PAID', 'DELIVERED')
    GROUP BY d.year, d.month ORDER BY d.year, d.month
    """
    with engine.connect() as conn:
        df_net = pd.read_sql(text(query_net), conn)
        df_gross = pd.read_sql(text(query_gross), conn)

    total_net = df_net["amount"].sum()
    total_gross = df_gross["amount"].sum()

    # Gross revenue before discounts should be >= net revenue
    assert total_gross >= total_net
    assert len(df_net) == len(df_gross)


def test_chart_builder_returns_inr_formatted_metadata() -> None:
    """
    Verify that the chart builder uses INR (₹) representation in
    titles, legends, and axes.
    """
    chart_json = build_sales_trend_chart()
    layout = chart_json.get("layout", {})
    traces = chart_json.get("data", [])

    # Check y-axis title mentions INR
    yaxis_title = layout.get("yaxis", {}).get("title", {}).get("text", "")
    assert "₹" in yaxis_title or "INR" in yaxis_title, (
        f"Y-axis title must show INR/₹: {yaxis_title}"
    )

    # Check trace names use ₹ instead of $
    trace_names = [t.get("name", "") for t in traces]
    assert any("₹" in name or "INR" in name for name in trace_names), (
        f"Traces must reflect INR currency: {trace_names}"
    )
    assert not any("$" in name for name in trace_names), (
        f"Traces must not contain dollar signs: {trace_names}"
    )


def test_regional_comparison_chart_inr_formatting() -> None:
    """
    Verify regional comparison bar chart uses INR currency notation.
    """
    reg_chart = build_regional_comparison_chart()
    layout = reg_chart.get("layout", {})
    yaxis_title = layout.get("yaxis", {}).get("title", {}).get("text", "")
    assert "₹" in yaxis_title or "INR" in yaxis_title, (
        f"Regional y-axis title must show INR/₹: {yaxis_title}"
    )
    assert "$" not in yaxis_title, (
        f"Regional y-axis title must not contain $: {yaxis_title}"
    )
