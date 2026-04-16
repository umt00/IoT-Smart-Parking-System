import pandas as pd
from sqlalchemy import create_engine, text
import os

current_dir = os.path.dirname(os.path.abspath(__file__))
csv_path = os.path.join(current_dir, '..', 'data', 'dataset.csv')
db_url = 'postgresql://postgres:umt123123..@localhost:5432/parking_db'
engine = create_engine(db_url)

df = pd.read_csv(csv_path)

df['event_time'] = pd.to_datetime(df['event_time'])
df['occupancy_rate'] = df['Occupancy'] / df['Capacity']
df['total_capacity'] = df['Capacity']
df['is_overflow'] = df['occupancy_rate'] > 1.0
df['is_holiday'] = df['event_time'].dt.dayofweek >= 5
df['is_exam_week'] = df['event_time'].apply(lambda x: True if x.month == 11 and 1 <= x.day <= 15 else False)
df['is_entry'] = True
df['is_raining'] = df['is_raining'].astype(bool)

columns_to_keep = [
    'occupancy_rate', 'total_capacity', 'is_entry', 'is_raining',
    'is_holiday', 'is_exam_week', 'is_overflow', 'event_time'
]
df_final = df[columns_to_keep].copy()

with engine.begin() as conn:
    conn.execute(text("TRUNCATE TABLE parking_entity RESTART IDENTITY CASCADE;"))

df_final.to_sql('parking_entity', engine, if_exists='append', index=False)