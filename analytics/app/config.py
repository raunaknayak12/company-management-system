import os
from pydantic import BaseModel

class Settings(BaseModel):
    DATABASE_URL: str = os.getenv("DATABASE_URL", "sqlite:///./cms_dw.db")
    INTERNAL_API_KEY: str = os.getenv("INTERNAL_API_KEY", "cms-internal-analytics-key-2026")
    DATA_DIR: str = os.getenv("DATA_DIR", os.path.join(os.path.dirname(__file__), "..", "data"))
    HOST: str = os.getenv("HOST", "0.0.0.0")
    PORT: int = int(os.getenv("PORT", "8000"))

settings = Settings()
