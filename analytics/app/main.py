import logging
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from .api.endpoints import router
from .etl.pipeline import run_etl_pipeline

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(name)s: %(message)s")
logger = logging.getLogger("cms_analytics")

app = FastAPI(
    title="CMS Analytics & ETL Engine",
    description="Python microservice providing ETL, Star-Schema OLAP querying, Holt-Winters forecasting, K-Means customer segmentation, TextBlob sentiment analysis, and Plotly/Seaborn visualization figures for Company Management System.",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(router)

@app.on_event("startup")
def startup_event():
    logger.info("Initializing CMS Analytics Engine...")
    try:
        # Run initial ETL to ensure warehouse data is hydrated and ready immediately
        run_etl_pipeline()
        logger.info("Initial ETL pipeline run completed successfully.")
    except Exception as e:
        logger.warning(f"Initial ETL run encountered issue: {e}")

if __name__ == "__main__":
    import uvicorn
    from .config import settings
    uvicorn.run("analytics.app.main:app", host=settings.HOST, port=settings.PORT, reload=True)
