package com.parking.parkingadvanced.service;

import com.parking.parkingadvanced.dto.WeatherResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private WeatherService weatherService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(weatherService, "weatherApiUrl", "http://dummy-weather-api.com");
        ReflectionTestUtils.setField(weatherService, "rainThreshold", 0.3);
    }

    @Test
    void fetchAndCacheDailyWeather_WhenApiFails_ShouldLoadFallbackCache() {
        when(restTemplate.getForObject(anyString(), eq(WeatherResponse.class)))
                .thenThrow(new RuntimeException("Weather API HTTP 500 Hatası"));

        weatherService.fetchAndCacheDailyWeather();

        List<Boolean> forecast = weatherService.getDailyRainForecast();

        assertEquals(24, forecast.size());
        assertFalse(forecast.get(0));
    }
}