import pandas as pd
from collections import Counter
import re
from ..db import engine

STOPWORDS = {
    "the", "and", "a", "to", "of", "in", "is", "for", "with", "our", "we", "this",
    "that", "it", "on", "as", "at", "by", "an", "be", "was", "are", "from", "or",
    "have", "has", "had", "not", "but", "very", "all", "so", "if", "too"
}

def compute_sentiment_analysis(product_id: int = None):
    query = """
    SELECT 
        fb.feedback_key,
        fb.rating,
        fb.review_text,
        fb.sentiment_label,
        fb.sentiment_score,
        d.year,
        d.month,
        d.month_name
    FROM fact_feedback fb
    JOIN dim_date d ON fb.date_key = d.date_key
    """
    if product_id:
        query += f" WHERE fb.product_key = {product_id}"
        
    with engine.connect() as conn:
        df = pd.read_sql(query, conn)
        
    if df.empty:
        return {
            "totalReviews": 0,
            "averageRating": 0.0,
            "sentimentDistribution": {"positive": 0, "neutral": 0, "negative": 0},
            "topKeywords": [],
            "ratingVsSentiment": [],
            "monthlyTrend": []
        }
        
    total_reviews = len(df)
    avg_rating = round(float(df["rating"].mean()), 2)
    
    # Sentiment Counts
    sentiment_counts = df["sentiment_label"].value_counts().to_dict()
    pos = int(sentiment_counts.get("POSITIVE", 0))
    neu = int(sentiment_counts.get("NEUTRAL", 0))
    neg = int(sentiment_counts.get("NEGATIVE", 0))
    
    # Top Keywords Extraction
    words = []
    for text_val in df["review_text"].dropna():
        cleaned = re.sub(r"[^\w\s]", "", str(text_val).lower())
        tokens = [w for w in cleaned.split() if len(w) > 3 and w not in STOPWORDS]
        words.extend(tokens)
        
    counter = Counter(words)
    top_keywords = [{"word": w, "count": c} for w, c in counter.most_common(12)]
    
    # Rating vs Sentiment
    rating_sentiment = []
    for r in range(1, 6):
        sub = df[df["rating"] == r]
        rating_sentiment.append({
            "rating": r,
            "count": len(sub),
            "avgSentiment": round(float(sub["sentiment_score"].mean()), 3) if not sub.empty else 0.0
        })
        
    # Monthly Sentiment Trend
    df["year_month"] = df["year"].astype(str) + "-" + df["month"].astype(str).str.zfill(2)
    monthly = df.groupby("year_month").agg({
        "sentiment_score": "mean",
        "rating": "mean",
        "feedback_key": "count"
    }).reset_index().rename(columns={"feedback_key": "reviews_count"})
    
    monthly_trend = []
    for _, row in monthly.iterrows():
        monthly_trend.append({
            "month": str(row["year_month"]),
            "avgSentiment": round(float(row["sentiment_score"]), 3),
            "avgRating": round(float(row["rating"]), 2),
            "count": int(row["reviews_count"])
        })
        
    return {
        "totalReviews": total_reviews,
        "averageRating": avg_rating,
        "sentimentDistribution": {
            "positive": pos,
            "neutral": neu,
            "negative": neg,
            "positivePct": round(pos / total_reviews * 100, 1),
            "neutralPct": round(neu / total_reviews * 100, 1),
            "negativePct": round(neg / total_reviews * 100, 1)
        },
        "topKeywords": top_keywords,
        "ratingVsSentiment": rating_sentiment,
        "monthlyTrend": monthly_trend
    }
