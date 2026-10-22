import pytest
from fastapi.testclient import TestClient
from analytics.app.main import app

client = TestClient(app)

def test_health():
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json()["status"] == "UP"

def test_etl_run():
    response = client.post("/etl/run", headers={"x-internal-key": "cms-internal-analytics-key-2026"})
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "SUCCESS"
    assert data["rowsLoadedSales"] > 0
    assert data["rowsLoadedFeedback"] > 0

def test_kpis():
    response = client.get("/analytics/kpis", headers={"x-internal-key": "cms-internal-analytics-key-2026"})
    assert response.status_code == 200
    data = response.json()
    assert "revenue" in data
    assert "profit" in data
    assert "totalOrders" in data

def test_forecast():
    response = client.get("/analytics/forecast?horizon=6", headers={"x-internal-key": "cms-internal-analytics-key-2026"})
    assert response.status_code == 200
    data = response.json()
    assert len(data["forecast"]) == 6
    assert "mape" in data

def test_segments():
    response = client.get("/analytics/segments?k=3", headers={"x-internal-key": "cms-internal-analytics-key-2026"})
    assert response.status_code == 200
    data = response.json()
    assert "points" in data
    assert "summary" in data

def test_sentiment():
    response = client.get("/analytics/sentiment", headers={"x-internal-key": "cms-internal-analytics-key-2026"})
    assert response.status_code == 200
    data = response.json()
    assert "sentimentDistribution" in data
    assert "topKeywords" in data
