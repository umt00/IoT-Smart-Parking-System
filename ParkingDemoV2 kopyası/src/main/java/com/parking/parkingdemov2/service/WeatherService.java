package com.parking.parkingdemov2.service;

import com.parking.parkingdemov2.dto.WeatherResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
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

    private final Map<Integer, Boolean> dailyRainCache = new ConcurrentHashMap<>();

    @PostConstruct
    @Scheduled(cron = "0 0 3 * * *")
    public void fetchAndCacheDailyWeather() {
        try {
            WeatherResponse response = restTemplate.getForObject(weatherApiUrl, WeatherResponse.class);

            if (response != null && response.getHourly() != null && response.getHourly().getRain() != null) {
                List<Double> rainData = response.getHourly().getRain();
                dailyRainCache.clear();

                for (int i = 0; i < 24 && i < rainData.size(); i++) {
                    dailyRainCache.put(i, rainData.get(i) > 0.1);
                }
                log.info("[JAVA]: Hava durumu verisi basariyla onbellege alindi.");
            } else {
                log.warn("[JAVA]: Hava durumu verisi bos geldi, fallback uygulaniyor.");
                loadFallbackWeather();
            }
        } catch (Exception e) {
            log.error("[JAVA HATA]: Hava durumu API baglanti sorunu: {}", e.getMessage());
            loadFallbackWeather();
        }
    }

    public boolean isRainingNow() {
        int currentHour = LocalDateTime.now().getHour();
        return dailyRainCache.getOrDefault(currentHour, false);
    }

    public List<Boolean> getDailyRainForecast() {
        List<Boolean> forecast = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            forecast.add(dailyRainCache.getOrDefault(i, false));
        }
        return forecast;
    }

    private void loadFallbackWeather() {
        dailyRainCache.clear();
        for (int i = 0; i < 24; i++) {
            dailyRainCache.put(i, false);
        }
    }
}