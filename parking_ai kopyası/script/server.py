from flask import Flask, request, jsonify
from sqlalchemy import create_engine
from sklearn.ensemble import RandomForestRegressor
from sklearn.model_selection import train_test_split
from sklearn.metrics import r2_score
import joblib
import numpy as np
import pandas as pd
import os
import threading
import json
from datetime import timedelta

current_dir = os.path.dirname(os.path.abspath(__file__))
model_path = os.path.join(current_dir, '..', 'models', 'model.pkl')
metrics_path = os.path.join(current_dir, '..', 'models', 'metrics.json')

db_url = 'postgresql://postgres:umt123123..@localhost:5432/parking_db'
engine = create_engine(db_url)

app = Flask(__name__)
model_lock = threading.Lock()
model = None

def load_metrics():
    if os.path.exists(metrics_path):
        with open(metrics_path, 'r') as f:
            return json.load(f)
    return {"r2_score": 0.0}

def save_metrics(score):
    with open(metrics_path, 'w') as f:
        json.dump({"r2_score": score}, f)

def train_and_save(df):
    global model
    df['hour'] = pd.to_datetime(df['event_time']).dt.hour
    df['hour_sin'] = np.sin(2 * np.pi * df['hour'] / 24)
    df['hour_cos'] = np.cos(2 * np.pi * df['hour'] / 24)

    X = df[['hour_sin', 'hour_cos', 'is_raining', 'is_holiday', 'is_exam_week']]
    y = df['occupancy_rate']

    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

    new_model = RandomForestRegressor(n_estimators=50, max_depth=10, random_state=42)
    new_model.fit(X_train, y_train)

    y_pred = new_model.predict(X_test)
    score = r2_score(y_test, y_pred)

    new_model.fit(X, y)

    with model_lock:
        joblib.dump(new_model, model_path)
        model = new_model
        save_metrics(score)

    return score

if os.path.exists(model_path):
    model = joblib.load(model_path)
else:
    try:
        query = "SELECT occupancy_rate, is_raining, is_holiday, is_exam_week, event_time FROM parking_entity"
        df_init = pd.read_sql(query, engine)
        if len(df_init) > 100:
            train_and_save(df_init)
    except:
        pass


@app.route('/predict/daily-batch', methods=['POST'])
def predict_daily():
    data = request.get_json()

    is_exam = data.get('isExamWeek', data.get('is_exam_week', False))
    is_holiday = data.get('isHoliday', data.get('is_holiday', False))
    hourly_weather = data.get('hourlyWeather', [False] * 24)

    predictions = []

    for hour in range(24):
        hour_sin = np.sin(2 * np.pi * hour / 24)
        hour_cos = np.cos(2 * np.pi * hour / 24)
        is_raining_current_hour = hourly_weather[hour] if hour < len(hourly_weather) else False

        features = pd.DataFrame([{
            'hour_sin': hour_sin,
            'hour_cos': hour_cos,
            'is_raining': is_raining_current_hour,
            'is_holiday': is_holiday,
            'is_exam_week': is_exam
        }])

        with model_lock:
            if model is not None:
                pred = model.predict(features)[0]
            else:
                pred = 0.0

        pred = max(0.0, float(pred))
        predictions.append(pred)

    metrics = load_metrics()
    return jsonify({
        "hourlyPredictions": predictions,
        "r2Score": metrics.get("r2_score", 0.0)
    })


@app.route('/train/retrain', methods=['POST'])
def retrain_model():
    try:
        query = "SELECT occupancy_rate, is_raining, is_holiday, is_exam_week, event_time FROM parking_entity"
        df = pd.read_sql(query, engine)

        if len(df) < 100:
            return jsonify({"status": "error", "message": "Yetersiz veri"}), 400

        df['event_time'] = pd.to_datetime(df['event_time'])
        four_weeks_ago = df['event_time'].max() - timedelta(weeks=4)
        df_filtered = df[df['event_time'] >= four_weeks_ago]

        if len(df_filtered) < 100:
            df_filtered = df

        score = train_and_save(df_filtered)

        return jsonify({"status": "success", "message": f"Model basariyla guncellendi. R2: {score:.4f}"})

    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500

if __name__ == '__main__':
    app.run(port=5001, host='0.0.0.0')