package com.parking.parkingadvanced.service;

import com.parking.parkingadvanced.dto.DailyPredictionResponseDto;
import com.parking.parkingadvanced.exception.PythonServiceException;
import com.parking.parkingadvanced.model.DailyPredictionEntity;
import com.parking.parkingadvanced.model.ParkingLotEntity;
import com.parking.parkingadvanced.repository.DailyPredictionRepository;
import com.parking.parkingadvanced.repository.ParkingLotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PredictionServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private DailyPredictionRepository predictionRepository;

    @Mock
    private ParkingLotRepository parkingLotRepository;

    @Mock
    private WeatherService weatherService;

    @Mock
    private AcademicCalendarService calendarService;

    @InjectMocks
    private PredictionService predictionService;

    @Test
    void getDailyForecast_WhenPythonFails_AndHistoryExists_ShouldReturnHistoricalData() {
        long lotId = 1L;
        LocalDate targetDate = LocalDate.now();
        LocalDate oneWeekAgo = targetDate.minusDays(7);

        ParkingLotEntity mockLot = ParkingLotEntity.builder().id(lotId).capacity(100).build();
        when(parkingLotRepository.findById(lotId)).thenReturn(Optional.of(mockLot));

        List<DailyPredictionEntity> historicalData = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            historicalData.add(DailyPredictionEntity.builder().predictionRate(0.85).build());
        }

        when(predictionRepository.findByParkingLot_IdAndTargetDateOrderByHourOfDayAsc(lotId, oneWeekAgo))
                .thenReturn(historicalData);

        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(new RuntimeException("Python Service Timeout/Connection Refused"));

        DailyPredictionResponseDto response = predictionService.getDailyForecast(lotId, targetDate);

        assertNotNull(response);
        assertEquals(24, response.getHourlyRates().size());
        assertEquals(85, response.getHourlyRates().get(0));
    }

    @Test
    void getDailyForecast_WhenPythonFails_AndNoHistory_ShouldThrowPythonServiceException() {
        long lotId = 1L;
        LocalDate targetDate = LocalDate.now();
        LocalDate oneWeekAgo = targetDate.minusDays(7);

        ParkingLotEntity mockLot = ParkingLotEntity.builder().id(lotId).capacity(100).build();
        when(parkingLotRepository.findById(lotId)).thenReturn(Optional.of(mockLot));

        when(predictionRepository.findByParkingLot_IdAndTargetDateOrderByHourOfDayAsc(lotId, oneWeekAgo))
                .thenReturn(Collections.emptyList());

        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(new RuntimeException("Python Down"));

        PythonServiceException thrownException = assertThrows(
                PythonServiceException.class,
                () -> predictionService.getDailyForecast(lotId, targetDate)
        );

        assertTrue(thrownException.getMessage().contains("Yapay zeka servisi geçici olarak devre dışı"));
    }
}