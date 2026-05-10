import pandas as pd
import numpy as np
from app.core.database import SessionLocal, engine, Base
from app.repositories.parking_repository import ParkingRepository
from app.models.domain import ParkingTransaction
from app.core.config import PARKING_CAPACITIES

def generate_mock_data_with_anomalies() -> pd.DataFrame:
    periods = 5000
    end_date = pd.Timestamp.now().floor('h')
    date_range = pd.date_range(end=end_date, periods=periods, freq='h')

    all_data = []

    hour_anchors = [0, 4, 7, 10, 13, 15, 18, 20, 23, 24]
    rate_anchors = [0.05, 0.02, 0.1, 0.95, 1.10, 1.05, 0.5, 0.2, 0.08, 0.05]

    for lot_id, capacity in PARKING_CAPACITIES.items():
        lot_df = pd.DataFrame({'event_time': date_range})
        lot_df["parking_lot_id"] = lot_id
        lot_df["capacity"] = capacity

        hours = lot_df['event_time'].dt.hour
        day_of_week = lot_df['event_time'].dt.dayofweek

        base_rate = np.interp(hours, hour_anchors, rate_anchors)

        lot_variance = np.random.uniform(-0.05, 0.05)
        base_rate = base_rate + lot_variance

        noise = np.random.normal(0, 0.15, periods)
        hidden_chaos = np.random.choice([0, 0.30, -0.30], size=periods, p=[0.90, 0.05, 0.05])
        base_rate = base_rate + noise + hidden_chaos

        lot_df["is_exam_week"] = np.random.choice([False, True], size=periods, p=[0.90, 0.10])
        lot_df["is_raining"] = np.random.choice([False, True], size=periods, p=[0.8, 0.2])
        lot_df["is_holiday"] = np.random.choice([False, True], size=periods, p=[0.95, 0.05])

        base_rate = np.where(lot_df["is_exam_week"], base_rate + 0.10, base_rate)
        base_rate = np.where(lot_df["is_raining"], base_rate + 0.05, base_rate)
        base_rate = np.where(lot_df["is_holiday"], base_rate * 0.10, base_rate)

        weekend_mask = day_of_week >= 5
        base_rate[weekend_mask] = base_rate[weekend_mask] * np.random.uniform(0.1, 0.2, sum(weekend_mask))

        base_rate = np.clip(base_rate, 0, None)

        lot_df["current_count"] = (base_rate * capacity).astype(int)
        lot_df["is_entry"] = True
        lot_df["is_overflow"] = lot_df["current_count"] > capacity

        all_data.append(lot_df)

    final_df = pd.concat(all_data).sort_values(by="event_time").reset_index(drop=True)

    columns_to_keep = [
        "parking_lot_id", "event_time", "current_count",
        "is_raining", "is_holiday", "is_exam_week",
        "is_entry", "is_overflow"
    ]
    return final_df[columns_to_keep]

def seed_database():
    Base.metadata.create_all(bind=engine)
    print("Kademeli simulasyon baslatiliyor...")
    df = generate_mock_data_with_anomalies()

    db = SessionLocal()
    repo = ParkingRepository(db, engine)

    print("Eski veritabani hareketleri temizleniyor...")
    db.query(ParkingTransaction).delete()
    db.commit()

    print(f"Toplam {len(df)} satir gercekci veri DB'ye yaziliyor...")
    repo.save_transactions(df)
    db.close()
    print("Basarili!")

if __name__ == "__main__":
    seed_database()