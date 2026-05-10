import os
from pydantic_settings import BaseSettings, SettingsConfigDict
from pydantic import field_validator


class Settings(BaseSettings):
    DATABASE_URL: str
    MODEL_PATH: str = "./models_storage/model.pkl"
    RAIN_THRESHOLD: float = 0.3

    @field_validator("DATABASE_URL", mode="before")
    @classmethod
    def fix_jdbc_and_quotes(cls, v: str) -> str:
        if not v:
            return v

        v = v.strip().replace('"', '').replace("'", "")

        if v.startswith("jdbc:postgresql://"):
            v = v.replace("jdbc:postgresql://", "postgresql://", 1)

        if "host.docker.internal" in v:
            v = v.replace("host.docker.internal", "halic_postgres")

        return v

    model_config = SettingsConfigDict(env_file=".env", extra="ignore")


settings = Settings()
PARKING_CAPACITIES = {1: 200, 2: 250, 3: 180, 4: 150}