from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from app.core.database import get_db, engine as db_engine
from app.repositories.parking_repository import ParkingRepository
from app.services.ml_service import MLService

router = APIRouter()

def get_ml_service(db: Session = Depends(get_db)):
    return MLService(ParkingRepository(db, db_engine))

@router.post("/retrain")
def trigger_retrain(ml_service: MLService = Depends(get_ml_service)):
    try:
        msg = ml_service.retrain_daily_model()
        return {"status": "success", "message": msg}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))