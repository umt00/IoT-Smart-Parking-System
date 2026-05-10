# Haliç Park — IoT & AI Destekli Akıllı Otopark Yönetim Sistemi

> **Java Spring Boot · Python FastAPI · Scikit-Learn · PostgreSQL · Docker**

Üniversite 2. sınıf dönem projesi olarak geliştirdiğim bu sistem; gerçek zamanlı IoT sensör verilerini, makine öğrenmesi tahminlerini ve akademik takvim/hava durumu entegrasyonunu tek bir çatı altında birleştiriyor. Proje, ilk sürümden bu yana büyük bir mimari dönüşüm geçirdi. Bunu "bitmiş" bir proje değil, gerçek mühendislik problemleriyle boğuşarak öğrendiğim canlı bir laboratuvar olarak görüyorum.

---

## Tanıtım


![Dashboard Görünümü](ss.png)
> Dashboard; anlık doluluk göstergeleri, 24 saatlik tahmin grafiği ve overflow uyarıları içeriyor.
> Arayüzü ben yazmadım — frontend benim güçlü alanım değil, bu kısımı yapay zeka desteğiyle tasarladım ve entegre ettim. Odağım tamamen backend ve AI pipeline üzerinde.

---

## Not

Öğrenmek ve hızlanmak için bazı bilinçli tercihler yaptım:

* **Frontend Meselesi:** Görsel arayüz benim güçlü alanım değil, açıkçası frontend üzerine uzmanlaşmak gibi bir derdim de yok. Bu yüzden dashboard tasarımını ve Chart.js kısmını tamamen **yapay zeka desteğiyle** hallettim. Benim odağım her zaman arka plandaki veri akışı (backend) ve AI pipeline oldu.
* **Test Yazma Süreci:** Test dünyasına yeni adım attım. Java tarafındaki JUnit ve Mockito testlerini yazarken yapay zekayı bir **mentör** gibi kullandım; bana mantığı öğretti, ben de koda döktüm. Python (FastAPI) tarafına da test eklemeyi hedefliyorum.
* **AI Mentörlüğü:** Projenin başından sonuna kadar yapay zekayı sadece kod kopyalamak için değil, "Neden bu mimariyi seçmeliyim?" veya "Docker neden bağlanmıyor?" gibi sorularla bir yol gösterici olarak kullandım.

---

## Sistem Mimarisi

```
[ESP32 Sensör] ──HTTP──▶ [Java Spring Boot :8080]
                               │         │
                    ┌──────────┘         └──────────┐
                    ▼                               ▼
           [PostgreSQL :5432]          [Python FastAPI :5000]
                                              │
                                    [RandomForest ML Model]
```

Üç bağımsız Docker container, tek bir `docker-compose` ağı üzerinde konuşuyor:

- **Java Spring Boot** — iş mantığı, sensör verisi alma, tahmin önbellekleme, hava durumu entegrasyonu
- **Python FastAPI** — model eğitimi, 24 saatlik tahmin üretimi
- **PostgreSQL** — tüm işlem ve tahmin verilerinin kalıcı depolanması

---

## Temel Özellikler

- **Gerçek Zamanlı Doluluk Takibi** — ESP32 sensörlerinden gelen her araç giriş/çıkışı anında işlenir ve kaydedilir.
- **24 Saatlik AI Tahmini** — RandomForest modeli, saat, gün, hava durumu, tatil ve sınav haftası değişkenlerini birleştirerek otopark başına saatlik doluluk oranı tahmin eder.
- **Champion/Challenger Model Değerlendirmesi** — Yeniden eğitilen model ancak mevcut modelin R² skorunu geçerse sisteme alınır; geçemezse reddedilir. Model asla gerilemez.
- **Otomatik Model Yenileme** — Her gece 04:00'te Spring Boot, Python servisini yeniden eğitim için tetikler.
- **Tahmin Önbellekleme** — Aynı güne ait tahminler DB'ye kaydedilir; Python'a ikinci kez istek atılmaz.
- **Üç Katmanlı Fallback** — Python çöküyor → geçen haftanın verileri döner → veri yoksa anlamlı hata mesajı üretilir.
- **Hava Durumu Entegrasyonu** — Open-Meteo API'den saatlik yağış verisi çekilir, tahminlere dahil edilir.
- **Akademik Takvim Desteği** — Sınav haftası ve resmi tatil verileri DB'den okunur, tahmin girdisi olarak kullanılır.
- **En İyi Otopark Tavsiyesi** — Anlık boş kapasite karşılaştırmasıyla en uygun otopark önerilir.
- **Race Condition Koruması** — Aynı anda iki sensör aynı otoparka yazarken çakışmayı önlemek için `PESSIMISTIC_WRITE` kilidi kullanılıyor.

---

## V2 İyileştirmeleri — Neler Değişti

Bu projenin önceki bir sürümü vardı; eski README'de Flask, manual kurulum ve çok daha basit bir yapı görünüyordu. V2'de ciddi bir mimari evrim yaşandı.

| Alan | V1 (Eski) | V2 (Bu Sürüm) |
|---|---|---|
| **Python çerçevesi** | Flask | FastAPI + Uvicorn |
| **Deployment** | Manual (`python main.py`) | Docker Compose (3 container) |
| **Uygulama başlatma** | Seed ve model eğitimi manuel | `lifespan` ile otomatik cold-start yönetimi |
| **Model kalitesi güvencesi** | Yok, her eğitim modeli değiştirir | Champion/Challenger R² karşılaştırması |
| **Fallback mekanizması** | Yok | 3 katmanlı (DB cache → geçmiş → hata) |
| **RestTemplate timeout** | 5 saniye (çok kısa) | 30 saniye okuma, 5 saniye bağlantı |
| **Yapılandırma doğrulama** | Yok | Pydantic `field_validator` ile JDBC URL düzeltme |
| **Veri üretimi** | Basit random | Gerçekçi anomali enjeksiyonlu simülasyon |

---

## Docker ile Çalıştırma

### Gereksinimler

- Docker Desktop (veya Docker Engine + Compose)
- Git

### Kurulum

```bash

git clone https://github.com/kullanici-adi/halic-park.git
cd halic-park
docker compose up --build
```

İlk başlatmada Python servisi otomatik olarak şunları yapar:
1. Veritabanı tabloları oluşturulur
2. 20.000 satırlık gerçekçi simülasyon verisi üretilip DB'ye yazılır
3. RandomForest modeli eğitilir ve `models_storage/` altına kaydedilir



Hazır olduğunda:
- **Dashboard:** http://localhost:8080
- **Python API Docs:** http://localhost:5000/docs

### Ortam Değişkenleri

`.env` dosyası oluştur (Python servisi için):

```env
DATABASE_URL=postgresql://parking_user:parking_pass@halic_postgres:5432/parking
MODEL_PATH=./models_storage/model.pkl
```

`application.properties` (Java servisi için) — gerekli anahtarlar:

```properties
spring.datasource.url=jdbc:postgresql://halic_postgres:5432/parking
spring.datasource.username=parking_user
spring.datasource.password=parking_pass

python.api.predict.url=http://python-service:5000/api/v1/parking
python.api.retrain.url=http://python-service:5000/api/v1/model/retrain

weather.api.url=https://api.open-meteo.com/...
app.api.key=SENSOR_SECRET_KEY
```

> **Not:** `host.docker.internal` içeren URL'ler Python servisi tarafından otomatik olarak `halic_postgres` adresine çevrilir. Bu, geliştirme sırasında karşılaştığımız en sinir bozucu sorunlardan biriydi.

---

## Teknoloji Stack'i

| Katman | Teknoloji |
|---|---|
| Backend (Java) | Java 17, Spring Boot 3, Spring Data JPA, Lombok, Maven |
| AI Microservice | Python 3.11, FastAPI, Uvicorn, Scikit-learn, Pandas, NumPy, SQLAlchemy 2.x, Pydantic v2 |
| Veritabanı | PostgreSQL 15 |
| Konteyner | Docker, Docker Compose |
| Dış API | Open-Meteo (hava durumu) |
| Frontend | HTML5, Chart.js (Dark Theme) — yapay zeka destekli |
| Test | JUnit 5, Mockito (Java) |

---

## API Referansı

### Java Servisi (`localhost:8080`)

| Method | Endpoint | Açıklama |
|---|---|---|
| `POST` | `/api/parking/sensor` | Sensör verisi al (X-API-KEY gerekli) |
| `GET` | `/api/parking/current/{id}` | Anlık doluluk |
| `GET` | `/api/parking/predictions/{id}?date=YYYY-MM-DD` | 24 saatlik tahmin |
| `GET` | `/api/parking/recommended` | En boş otopark tavsiyesi |

### Python Servisi (`localhost:5000`)

| Method | Endpoint | Açıklama |
|---|---|---|
| `POST` | `/api/v1/parking/predict/{lot_id}` | 24 saatlik tahmin üret |
| `POST` | `/api/v1/model/retrain` | Modeli yeniden eğit |
| `GET` | `/docs` | Swagger UI |

---

## 🔐 Güvenlik

Şu an yalnızca sensör yazma endpoint'i (`/api/parking/sensor`) API key ile korunuyor. Bu bilinçli bir karar — diğer endpoint'ler okuma amaçlı ve halka açık bir dashboard için tasarlandı.

**Eksik ve planlanan:**

- [ ] Spring Security ile tam authentication katmanı
- [ ] JWT tabanlı kullanıcı yönetimi
- [ ] Python servisine iç ağ dışında erişim engeli
- [ ] Rate limiting

---

## Karşılaşılan Zorluklar

**Bağlantı hataları:** Özeliikle Java ve Python servisleri arasındaki HTTP iletişimde 'Connection refused' hataları aldık. Bu hataları çözmek günler sürdü. 

**Docker içinde localhost izolasyonu:** Java ve Python servisleri Docker container'ları olduğundan `localhost` birbirini görmüyor. `application.properties`'deki `localhost:5432` geliştirme sırasında çalışıyor ama container içinde çöküyordu. Çözüm: container adlarını (`halic_postgres`, `python-service`) kullanmak. Python tarafında ise `field_validator` ile JDBC URL'deki `host.docker.internal`'ı otomatik düzeltiyoruz — bu düzeltme kodda hâlâ duruyor.

**Frontend-Backend yuvarlama hatası:** Python modeli `0.85` gibi bir float döndürüyor, Java bunu `%85` olarak frontend'e gönderiyor. Ama `Math.round(0.849999... * 100)` = `84` olabiliyor. Özellikle `double` hassasiyetinde küçük kayıplar UI'da yanlış göstergelere yol açıyordu. Çözüm: tahmin değerlerini erken normalize etmek.

**Cold-start problemi:** İlk `docker compose up`'ta DB boş ve model dosyası yok. Python servisi model olmadan tahmin yapamıyor. Çözüm: FastAPI'nin `lifespan` mekanizmasıyla startup sırasında sırayla seed → eğitim yapıyoruz.

**Saat özelliğinin döngüsel doğası:** 23 ile 0 arasındaki geçiş, modele ham sayı olarak verildiğinde çok büyük bir fark gibi görünüyor. `sin/cos` dönüşümüyle bu döngüsel ilişkiyi modele doğru şekilde öğrettik.

---

## Proje Yapısı

```
halic-park/
├── docker-compose.yml
├── Python/
│   └── ParkingAdvancedPython/
│       ├── Dockerfile
│       ├── main.py                    # FastAPI app + lifespan startup
│       ├── .env
│       └── app/
│           ├── api/                   # predict.py, train.py
│           ├── core/                  # config.py, database.py
│           ├── models/               # domain.py (SQLAlchemy), schemas.py (Pydantic)
│           ├── pipeline/             # data_seeder.py
│           ├── preprocessing/        # features.py, cleaner.py
│           ├── repositories/         # parking_repository.py
│           └── services/             # ml_service.py
└── Java/
    └── ParkingAdvancedJava/
        ├── Dockerfile
        ├── pom.xml
        └── src/main/java/.../
            ├── config/               # ParkingLotConfig, RestTemplateConfig
            ├── controller/           # ParkingController
            ├── dto/                  # Request/Response DTO'lar
            ├── exception/            # GlobalExceptionHandler
            ├── init/                 # DataBaseSeeder
            ├── model/                # JPA entity'leri
            ├── repository/           # Spring Data JPA repository'leri
            ├── security/             # ApiKeyFilter
            └── service/              # ParkingTransactionService, PredictionService,
                                      # WeatherService, AcademicCalendarService
```


---

## Gelecek Planları

- [ ] Spring Security + JWT ile tam auth katmanı
- [ ] Python servisinde pytest entegrasyonu
- [ ] Gerçek ESP32 donanım entegrasyonu (şu an simüle ediliyor)
- [ ] Model açıklanabilirliği (SHAP değerleri)
- [ ] Prometheus + Grafana metrik izleme (controller'da yorum satırında duruyor)
- [ ] `PARKING_CAPACITIES` sabitini DB'den okuyacak şekilde refactor

---

*2. sınıf öğrencisi olarak bu projeyi tamamlamak, bana sadece kod yazmayı değil; sistemlerin neden çöktüğünü, containerların nasıl konuştuğunu ve bir ML modelinin production'a taşınmasının teoriden ne kadar farklı olduğunu öğretti.*
