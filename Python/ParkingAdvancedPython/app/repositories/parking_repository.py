from sqlalchemy.orm import Session
from sqlalchemy import Engine
from datetime import datetime, timedelta
import pandas as pd
from app.models.domain import ParkingTransaction
from app.core.config import PARKING_CAPACITIES
from sqlalchemy import Engine

class ParkingRepository:
    def __init__(self, db: Session, engine: Engine):
        self.db = db
        self.engine = engine

    def get_last_n_days_data_as_df(self, days: int = 30) -> pd.DataFrame:
        target_date = datetime.now() - timedelta(days=days)
        query = self.db.query(ParkingTransaction).filter(
            ParkingTransaction.event_time >= target_date
        ).statement
        with self.engine.connect() as conn:
            return pd.read_sql(query, conn)

    def save_transactions(self, df: pd.DataFrame):
        with self.engine.begin() as conn:
            df.to_sql(
                name=ParkingTransaction.__tablename__,
                con=conn,
                if_exists="append",
                index=False
            )

            
    def get_occupancy_rate_ago(self, lot_id: int, hours_ago: int) -> float:
        target_time = datetime.now() - timedelta(hours=hours_ago)

        record = self.db.query(ParkingTransaction) \
            .filter(ParkingTransaction.parking_lot_id == lot_id) \
            .filter(ParkingTransaction.event_time <= target_time) \
            .order_by(ParkingTransaction.event_time.desc()) \
            .first()

        if record:
            cap = PARKING_CAPACITIES.get(lot_id, 200)
            return record.current_count / cap

        return 0.5