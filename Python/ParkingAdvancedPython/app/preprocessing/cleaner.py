import pandas as pd

def clean_data(df: pd.DataFrame) -> pd.DataFrame:
    df = df.dropna(subset=["occupancy_rate"])
    df = df[(df["occupancy_rate"] >= 0) & (df["occupancy_rate"] <= 1.5)]
    return df.sort_values(by=["parking_lot_id", "event_time"]).reset_index(drop=True)