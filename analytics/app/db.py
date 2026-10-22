import os
import logging
from sqlalchemy import create_engine, text
from sqlalchemy.orm import sessionmaker
from .config import settings

logger = logging.getLogger("cms_analytics.db")

def get_engine():
    db_url = settings.DATABASE_URL
    try:
        # Check if Postgres is reachable, if not fallback to SQLite
        if db_url.startswith("postgresql"):
            engine = create_engine(db_url, pool_pre_ping=True)
            with engine.connect() as conn:
                conn.execute(text("SELECT 1"))
            logger.info("Successfully connected to PostgreSQL warehouse.")
            return engine
    except Exception as e:
        logger.warning(f"PostgreSQL connection failed ({e}). Falling back to local SQLite warehouse.")
    
    sqlite_path = os.path.join(os.path.dirname(__file__), "..", "cms_dw.db")
    return create_engine(f"sqlite:///{sqlite_path}")

engine = get_engine()
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
