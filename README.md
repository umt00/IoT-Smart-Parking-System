# IoT & AI Supported Smart Parking Management System

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Python](https://img.shields.io/badge/Python-3776AB?style=for-the-badge&logo=python&logoColor=white)
![Scikit-Learn](https://img.shields.io/badge/scikit--learn-%23F7931E.svg?style=for-the-badge&logo=scikit-learn&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![ESP32](https://img.shields.io/badge/ESP32-000000?style=for-the-badge&logo=espressif&logoColor=white)

> **An intelligent, data-driven, and autonomous parking management solution designed for modern smart cities.**

---

## Project Overview
The **Smart Parking Management System** is a full-stack engineering project that integrates physical IoT hardware with advanced Machine Learning algorithms. The system monitors parking occupancy in real-time and provides predictive availability models based on weather conditions, historical data, and academic calendars.

##  System Architecture
The application follows a modular 3-tier architecture:
1. **IoT Edge Layer:** ESP32 microcontrollers with ultrasonic distance sensors monitor individual slot occupancy and transmit state changes via HTTP.
2. **Core Backend (Java):** A robust Spring Boot service managing business logic, persistent data (PostgreSQL/Hibernate), and external API integrations (Open-Meteo).
3. **AI Microservice (Python):** A dedicated Flask API hosting a Random Forest model to forecast future occupancy trends.

## Key Features
- **Real-Time Occupancy Tracking:** Live status updates from physical sensors.
- **Predictive Modeling:** AI-driven forecasting for future parking availability.
- **Microservice Communication:** Seamless RESTful integration between Java and Python services.
- **Environmental Awareness:** Dynamic predictions adjusted for real-time weather data.

## Engineering Challenges & Solutions
- **Challenge:** High signal noise and inconsistent readings from IoT sensors.
  - **Solution:** Engineered a software-based debounce and validation filter in the backend to ensure data integrity.
- **Challenge:** Managing cross-platform data synchronization.
  - **Solution:** Implemented a standardized JSON-based communication protocol for real-time AI inference.

## Technology Stack
| Layer | Technologies |
| :--- | :--- |
| **Backend** | Java 17, Spring Boot, Spring Data JPA, Maven |
| **AI/ML** | Python 3, Flask, Scikit-learn, Pandas |
| **Database** | PostgreSQL |
| **Hardware** | ESP32, Ultrasonic Sensors |

## Installation & Setup

### 1. Database Setup
Create a PostgreSQL database named `parking_db` and update `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/parking_db
spring.datasource.username=postgres
spring.datasource.password=YOUR_PASSWORD
 ```

## AI Microservice Execution
```
cd parking_ai
pip install -r requirements.txt
python main.py
```

## Java Backend Execution
```
cd ParkingDemoV2
mvn clean install
mvn spring-boot:run
```