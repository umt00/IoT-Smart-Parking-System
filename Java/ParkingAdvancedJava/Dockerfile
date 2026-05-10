# --- AŞAMA 1: Build (Derleme) ---
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /app

# Sadece pom.xml'i kopyalayıp bağımlılıkları indiriyoruz
COPY pom.xml .
COPY src ./src

# Testleri atlayarak .jar dosyasını oluşturuyoruz
RUN mvn clean package -Dmaven.test.skip=true


# --- AŞAMA 2: Run (Çalıştırma) ---
FROM eclipse-temurin:17-jre
WORKDIR /app

# Birinci aşamadan sadece üretilen .jar dosyasını çekiyoruz
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

