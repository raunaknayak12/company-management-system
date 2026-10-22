import io
import base64
import pandas as pd
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import seaborn as sns
from ..db import engine

def generate_seaborn_plots():
    query = """
    SELECT 
        f.quantity,
        f.unit_price,
        f.unit_cost,
        f.discount_percent,
        f.net_amount,
        f.profit_amount,
        p.category,
        f.region
    FROM fact_sales_order f
    JOIN dim_product p ON f.product_key = p.product_key
    WHERE f.order_status IN ('CONFIRMED', 'PAID', 'DELIVERED')
    """
    with engine.connect() as conn:
        df = pd.read_sql(query, conn)
        
    if df.empty:
        return {"correlationHeatmap": "", "categoryBoxPlot": ""}
        
    # 1. Correlation Heatmap
    plt.figure(figsize=(7, 5))
    numeric_df = df[["quantity", "unit_price", "discount_percent", "net_amount", "profit_amount"]]
    corr = numeric_df.corr()
    sns.heatmap(corr, annot=True, cmap="coolwarm", fmt=".2f", linewidths=0.5, cbar=True)
    plt.title("Financial Metrics Correlation Matrix", fontsize=12, pad=12)
    plt.tight_layout()
    
    buf_corr = io.BytesIO()
    plt.savefig(buf_corr, format="png", dpi=120)
    plt.close()
    buf_corr.seek(0)
    b64_corr = base64.b64encode(buf_corr.read()).decode("utf-8")
    
    # 2. Boxplot: Profit Distribution by Product Category
    plt.figure(figsize=(8, 5))
    sns.boxplot(x="category", y="profit_amount", data=df, palette="Set2")
    plt.title("Net Profit Spread by Product Category", fontsize=12, pad=12)
    plt.xlabel("Category")
    plt.ylabel("Profit Amount ($)")
    plt.xticks(rotation=15)
    plt.tight_layout()
    
    buf_box = io.BytesIO()
    plt.savefig(buf_box, format="png", dpi=120)
    plt.close()
    buf_box.seek(0)
    b64_box = base64.b64encode(buf_box.read()).decode("utf-8")
    
    return {
        "correlationHeatmap": f"data:image/png;base64,{b64_corr}",
        "categoryBoxPlot": f"data:image/png;base64,{b64_box}"
    }
