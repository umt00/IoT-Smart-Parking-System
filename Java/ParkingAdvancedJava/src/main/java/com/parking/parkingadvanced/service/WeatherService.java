package com.parking.parkingadvanced.service;

import com.parking.parkingadvanced.dto.WeatherResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

    private final RestTemplate restTemplate;

    @Value("${weather.api.url}")
    private String weatherApiUrl;

    @Value("${weather.rain-threshold:0.3}")
    private double rainThreshold;

    private final Map<String, Boolean> dailyRainCache = new ConcurrentHashMap<>();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH");

    @PostConstruct
    @Scheduled(cron = "0 0 3 * * *")
    public void fetchAndCacheDailyWeather() {
        try {
            WeatherResponse response = restTemplate.getForObject(weatherApiUrl, WeatherResponse.class);
            if (response != null && response.getHourly() != null && response.getHourly().getRain() != null) {

                Map<String, Boolean> newCache = new ConcurrentHashMap<>();
                List<Double> rainData = response.getHourly().getRain();
                LocalDate today = LocalDate.now();

                for (int i = 0; i < 24 && i < rainData.size(); i++) {
                    String key = today.atTime(i, 0).format(formatter);
                    newCache.put(key, rainData.get(i) > rainThreshold);
                }

                dailyRainCache.clear();
                dailyRainCache.putAll(newCache);

                log.info("[JAVA]: Hava durumu verisi kesintisiz güncellendi.");
            } else {
                log.warn("[JAVA]: Hava durumu verisi bos geldi, fallback uygulaniyor.");
                loadFallbackWeather();
            }
        } catch (Exception e) {
            log.error("[JAVA HATA]: API hatası, eski veriler korunuyor. {}", e.getMessage());
            loadFallbackWeather();
        }
    }

    public boolean isRainingNow() {
        String currentKey = LocalDateTime.now().format(formatter);
        return dailyRainCache.getOrDefault(currentKey, false);
    }

    public List<Boolean> getDailyRainForecast() {
        List<Boolean> forecast = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 24; i++) {
            String key = today.atTime(i, 0).format(formatter);
            forecast.add(dailyRainCache.getOrDefault(key, false));
        }
        return forecast;
    }

    private void loadFallbackWeather() {
        Map<String, Boolean> fallbackCache = new ConcurrentHashMap<>();
        LocalDate today = LocalDate.now();

        for (int i = 0; i < 24; i++) {
            String key = today.atTime(i, 0).format(formatter);
            fallbackCache.put(key, false);
        }

        dailyRainCache.clear();
        dailyRainCache.putAll(fallbackCache);
    }
}