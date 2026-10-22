import pandas as pd
import numpy as np
from sklearn.cluster import KMeans
from sklearn.preprocessing import StandardScaler
from ..db import engine

def compute_customer_segments(k_clusters: int = 4):
    k_clusters = max(2, min(k_clusters, 8))
    
    query = """
    SELECT 
        c.customer_key,
        c.company_name,
        c.region,
        COUNT(DISTINCT f.order_id) as frequency,
        SUM(f.net_amount) as monetary,
        MAX(d.full_date) as last_date
    FROM fact_sales_order f
    JOIN dim_customer c ON f.customer_key = c.customer_key
    JOIN dim_date d ON f.date_key = d.date_key
    WHERE f.order_status IN ('CONFIRMED', 'PAID', 'DELIVERED')
    GROUP BY c.customer_key, c.company_name, c.region
    """
    
    with engine.connect() as conn:
        df = pd.read_sql(query, conn)
        
    if df.empty or len(df) < k_clusters:
        return {"clusters": [], "summary": []}
        
    df["last_date"] = pd.to_datetime(df["last_date"])
    max_date = df["last_date"].max()
    df["recency_days"] = (max_date - df["last_date"]).dt.days
    
    # Feature matrix: Recency, Frequency, Monetary
    features = df[["recency_days", "frequency", "monetary"]].copy()
    scaler = StandardScaler()
    scaled_features = scaler.fit_transform(features)
    
    kmeans = KMeans(n_clusters=k_clusters, random_state=42, n_init=10)
    df["cluster"] = kmeans.fit_predict(scaled_features)
    
    # Assign intuitive cluster labels
    cluster_stats = df.groupby("cluster").agg({
        "recency_days": "mean",
        "frequency": "mean",
        "monetary": "mean"
    })
    
    # Sort by monetary descending
    ranked_clusters = cluster_stats.sort_values(by="monetary", ascending=False).index.tolist()
    labels = {}
    preset_names = ["VIP Champions", "Loyal High-Value", "Growing Potential", "At-Risk / Inactive", "Standard Customers", "Occasional Buyers", "New Prospects", "Dormant"]
    for i, cluster_id in enumerate(ranked_clusters):
        labels[cluster_id] = preset_names[i] if i < len(preset_names) else f"Segment {cluster_id + 1}"
        
    df["segment_name"] = df["cluster"].map(labels)
    
    # Summary Table
    summary = []
    for c_id in range(k_clusters):
        sub = df[df["cluster"] == c_id]
        summary.append({
            "clusterId": c_id,
            "segmentName": labels.get(c_id, f"Segment {c_id}"),
            "customerCount": int(len(sub)),
            "avgRecencyDays": round(float(sub["recency_days"].mean()), 1),
            "avgFrequencyOrders": round(float(sub["frequency"].mean()), 1),
            "avgMonetarySpend": round(float(sub["monetary"].mean()), 2),
            "totalRevenueContribution": round(float(sub["monetary"].sum()), 2)
        })
        
    # Scatter data points for Plotly rendering
    points = []
    for _, row in df.iterrows():
        points.append({
            "customerKey": int(row["customer_key"]),
            "companyName": str(row["company_name"]),
            "region": str(row["region"]),
            "recency": int(row["recency_days"]),
            "frequency": int(row["frequency"]),
            "monetary": round(float(row["monetary"]), 2),
            "cluster": int(row["cluster"]),
            "segmentName": str(row["segment_name"])
        })
        
    return {
        "k": k_clusters,
        "points": points,
        "summary": summary
    }
