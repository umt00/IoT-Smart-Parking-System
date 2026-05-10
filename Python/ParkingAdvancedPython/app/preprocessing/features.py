import pandas as pd
import numpy as np
from app.models.schemas import PredictionRequest
from app.core.config import PARKING_CAPACITIES


def extract_features(df: pd.DataFrame) -> pd.DataFrame:
    df = df.copy()
    if "current_count" in df.columns:
        df['capacity'] = df['parking_lot_id'].map(PARKING_CAPACITIES)
        df['occupancy_rate'] = df['current_count'] / df['capacity']

    df['hour'] = pd.to_datetime(df['event_time']).dt.hour
    df['hour_sin'] = np.sin(2 * np.pi * df['hour'] / 24)
    df['hour_cos'] = np.cos(2 * np.pi * df['hour'] / 24)
    df['day_of_week'] = pd.to_datetime(df['event_time']).dt.dayofweek

    for lot_id in PARKING_CAPACITIES.keys():
        df[f'is_lot_{lot_id}'] = (df['parking_lot_id'] == lot_id).astype(int)

    return df


def prepare_inference_features(lot_id: int, request: PredictionRequest) -> pd.DataFrame:
    target_date = pd.to_datetime(request.targetDate)

    df = pd.DataFrame({
        'parking_lot_id': [lot_id] * 24,
        'hour': range(24),
        'is_raining': request.dailyWeather,
        'is_holiday': [request.isHoliday] * 24,
        'is_exam_week': [request.isExam] * 24
    })

    df['hour_sin'] = np.sin(2 * np.pi * df['hour'] / 24)
    df['hour_cos'] = np.cos(2 * np.pi * df['hour'] / 24)
    df['day_of_week'] = target_date.dayofweek

    for c_lot_id in PARKING_CAPACITIES.keys():
        df[f'is_lot_{c_lot_id}'] = (df['parking_lot_id'] == c_lot_id).astype(int)

    features = [
                   'hour_sin', 'hour_cos', 'day_of_week',
                   'is_raining', 'is_holiday', 'is_exam_week'
               ] + [f'is_lot_{id}' for id in PARKING_CAPACITIES.keys()]

    return df[features]