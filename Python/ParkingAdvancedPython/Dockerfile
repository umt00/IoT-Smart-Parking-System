# 1. Temel imaj: Hafif bir Python 3.10 sürümü
FROM python:3.11-slim

# 2. Çalışma dizini oluşturuluyor
WORKDIR /app

# 3. Önce kütüphane listesini alıp kuruyoruz (Docker cache avantajı için)
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 4. Projenin kalan tüm dosyalarını kopyalıyoruz
COPY . .

# 5. FastAPI'nin çalışacağı port
EXPOSE 5000

# 6. Konteyner başlatıldığında çalışacak komut
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "5000"]