import logging
from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from .api.endpoints import router
from .etl.pipeline import run_etl_pipeline

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(name)s: %(message)s")
logger = logging.getLogger("cms_analytics")

@asynccontextmanager
async def lifespan(app: FastAPI):
    """Application lifespan: runs ETL on startup, cleans up on shutdown."""
    logger.info("Initializing CMS Analytics Engine...")
    try:
        run_etl_pipeline()
        logger.info("Initial ETL pipeline run completed successfully.")
    except Exception as e:
        logger.warning(f"Initial ETL run encountered issue: {e}")
    yield
    logger.info("CMS Analytics Engine shutting down.")

app = FastAPI(
    title="CMS Analytics & ETL Engine",
    description="Python microservice providing ETL, Star-Schema OLAP querying, Holt-Winters forecasting, K-Means customer segmentation, TextBlob sentiment analysis, and Plotly/Seaborn visualization figures for Company Management System.",
    version="1.0.0",
    lifespan=lifespan,
)

import os

origins_env = os.getenv("CORS_ALLOWED_ORIGINS", "http://localhost:8080,http://127.0.0.1:8080")
allowed_origins = [orig.strip() for orig in origins_env.split(",") if orig.strip()]

app.add_middleware(
    CORSMiddleware,
    allow_origins=allowed_origins,
    allow_credentials=True,
    allow_methods=["GET", "POST", "PUT", "DELETE", "OPTIONS"],
    allow_headers=["*"],
)

app.include_router(router)

if __name__ == "__main__":
    import uvicorn
    from .config import settings
    uvicorn.run("app.main:app", host=settings.HOST, port=settings.PORT, reload=True)
