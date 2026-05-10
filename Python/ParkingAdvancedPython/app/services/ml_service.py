import joblib
import os
import json
from datetime import datetime
from sklearn.ensemble import RandomForestRegressor
from sklearn.metrics import r2_score

from app.core.config import settings, PARKING_CAPACITIES
from app.repositories.parking_repository import ParkingRepository
from app.models.schemas import PredictionRequest
from app.preprocessing.cleaner import clean_data
from app.preprocessing.features import extract_features, prepare_inference_features

class MLService:
    def __init__(self, repository: ParkingRepository):
        self.repository = repository
        self.model_dir = os.path.dirname(settings.MODEL_PATH)
        self.metadata_path = os.path.join(str(self.model_dir), "metadata.json")
        self.current_model, self.model_version = self._load_latest_model()
        self.current_r2_score = self._load_latest_score()

    def _load_latest_score(self) -> float:
        if os.path.exists(self.metadata_path):
            try:
                with open(self.metadata_path, 'r') as f:
                    metadata = json.load(f)
                    return metadata.get("r2_score", 0.60)
            except Exception as e:
                print(f"Metadata okunamadi: {e}")
        return 0.60

    def _load_latest_model(self):
        if not os.path.exists(self.model_dir):
            return None, None
        files = [f for f in os.listdir(self.model_dir) if f.endswith('.pkl')]
        if not files:
            return None, None
        latest_file = sorted(files)[-1]
        return joblib.load(os.path.join(self.model_dir, latest_file)), latest_file

    def retrain_daily_model(self) -> str:
        df = self.repository.get_last_n_days_data_as_df(30)
        df = extract_features(df)
        df = clean_data(df)

        if len(df) < 100:
            raise ValueError("Yetersiz veri (Minimum 100 satir gerekli).")

        features = [
            'hour_sin', 'hour_cos', 'day_of_week',
            'is_raining', 'is_holiday', 'is_exam_week'
        ] + [f'is_lot_{id}' for id in PARKING_CAPACITIES.keys()]

        df = df.sort_values(by="event_time").reset_index(drop=True)
        split_idx = int(len(df) * 0.8)

        train_df = df.iloc[:split_idx]
        test_df = df.iloc[split_idx:]

        X_train, y_train = train_df[features], train_df['occupancy_rate']
        X_test, y_test = test_df[features], test_df['occupancy_rate']

        new_model = RandomForestRegressor(n_estimators=50, max_depth=10, random_state=42)
        new_model.fit(X_train, y_train)

        new_score = r2_score(y_test, new_model.predict(X_test))

        if new_score >= self.current_r2_score:
            new_model.fit(df[features], df['occupancy_rate'])
            os.makedirs(self.model_dir, exist_ok=True)

            version = datetime.now().strftime("%Y%m%d_%H%M%S")
            filename = f"model_rf_{version}.pkl"

            joblib.dump(new_model, os.path.join(self.model_dir, filename))

            metadata = {
                "version": version,
                "filename": filename,
                "r2_score": new_score,
                "trained_at": datetime.now().isoformat()
            }
            with open(self.metadata_path, 'w') as f:
                json.dump(metadata, f, indent=4)

            self.current_model = new_model
            self.current_r2_score = new_score
            return f"BASARILI: Yeni model yeterli. R2 Skoru: {new_score:.4f}"

        return f"REDDEDILDI: Yeni model yetersiz. Eski modelle devam ediliyor. R2: {new_score:.4f}"

    def predict_24_hours(self, lot_id: int, request: PredictionRequest) -> list[float]:
        if not self.current_model:
            raise ValueError("Sistemde egitilmis bir model bulunamadi.")

        X_pred = prepare_inference_features(lot_id, request)

        predictions = self.current_model.predict(X_pred)
        return [float(p) for p in predictions]