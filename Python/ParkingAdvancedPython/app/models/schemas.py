from typing import List
from pydantic import BaseModel
from datetime import datetime

class PredictionRequest(BaseModel):
    targetDate: datetime
    isExam: bool = False
    isHoliday: bool = False
    dailyWeather: List[bool]

    class Config:
        extra = "ignore"

class PredictionResponse(BaseModel):
    hourlyPredictions: List[float]
    status: str = "success"