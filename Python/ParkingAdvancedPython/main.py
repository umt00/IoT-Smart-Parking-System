import os
import uvicorn
from contextlib import asynccontextmanager
from fastapi import FastAPI
from app.core.config import settings
from app.core.database import Base, engine, SessionLocal
from app.models.domain import ParkingTransaction
from app.repositories.parking_repository import ParkingRepository
from app.services.ml_service import MLService
from app.api import predict, train

@asynccontextmanager
async def lifespan(app: FastAPI):
    Base.metadata.create_all(bind=engine)

    db = SessionLocal()
    count = db.query(ParkingTransaction).count()
    db.close()

    if count == 0:
        print("[STARTUP] DB boş, seed başlatılıyor...")
        from app.pipeline.data_seeder import seed_database
        seed_database()

    model_dir = os.path.dirname(settings.MODEL_PATH)
    has_model = os.path.exists(model_dir) and any(
        f.endswith('.pkl') for f in os.listdir(model_dir)
    )

    if not has_model:
        print("[STARTUP] Model bulunamadı, eğitim başlatılıyor...")
        db = SessionLocal()
        repo = ParkingRepository(db, engine)
        ml = MLService(repo)
        try:
            ml.retrain_daily_model()
        except Exception as e:
            print(f"[STARTUP HATA] Model eğitilemedi: {e}")
        finally:
            db.close()

    yield

app = FastAPI(title="HALIC PARKİNG AI", version="2.0.0", lifespan=lifespan)

app.include_router(train.router, prefix="/api/v1/model", tags=["Model"])
app.include_router(predict.router, prefix="/api/v1/parking", tags=["Prediction"])

if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=5000, reload=False)