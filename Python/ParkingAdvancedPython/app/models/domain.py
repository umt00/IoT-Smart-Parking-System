from sqlalchemy import Column, Integer, Boolean, DateTime
from app.core.database import Base

class ParkingTransaction(Base):
    __tablename__ = "parking_transaction_entity"

    id = Column(Integer, primary_key=True, index=True)
    parking_lot_id = Column(Integer, index=True)
    current_count = Column(Integer)
    is_raining = Column(Boolean)
    is_holiday = Column(Boolean)
    is_exam_week = Column(Boolean)
    is_overflow = Column(Boolean)
    event_time = Column(DateTime)
    is_entry = Column(Boolean)