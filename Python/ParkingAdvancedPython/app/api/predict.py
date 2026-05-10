from fastapi import APIRouter, HTTPException, Depends
from sqlalchemy.orm import Session
from app.models.schemas import PredictionRequest, PredictionResponse
from app.services.ml_service import MLService
from app.repositories.parking_repository import ParkingRepository
from app.core.database import get_db, engine as db_engine

router = APIRouter()

def get_ml_service(db: Session = Depends(get_db)):
    return MLService(ParkingRepository(db, db_engine))

@router.post("/predict/{lot_id}", response_model=PredictionResponse)
def get_predictions(lot_id: int, request: PredictionRequest, ml_service: MLService = Depends(get_ml_service)):
    try:
        preds = ml_service.predict_24_hours(lot_id, request)
        return PredictionResponse(hourlyPredictions=preds)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))