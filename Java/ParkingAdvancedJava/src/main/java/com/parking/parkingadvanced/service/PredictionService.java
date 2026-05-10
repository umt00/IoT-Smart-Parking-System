package com.parking.parkingadvanced.service;

import com.parking.parkingadvanced.dto.DailyPredictionResponseDto;
import com.parking.parkingadvanced.dto.PredictionRequestDto;
import com.parking.parkingadvanced.dto.PredictionResponseDto;
import com.parking.parkingadvanced.exception.PythonServiceException;
import com.parking.parkingadvanced.exception.ResourceNotFoundException;
import com.parking.parkingadvanced.model.DailyPredictionEntity;
import com.parking.parkingadvanced.model.ParkingLotEntity;
import com.parking.parkingadvanced.repository.DailyPredictionRepository;
import com.parking.parkingadvanced.repository.ParkingLotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PredictionService {

    private final RestTemplate restTemplate;
    private final WeatherService weatherService;
    private final AcademicCalendarService calendarService;
    private final DailyPredictionRepository predictionRepository;
    private final ParkingLotRepository parkingLotRepository;

    @Value("${python.api.predict.url}")
    private String pythonPredictUrl;

    @Value("${python.api.retrain.url}")
    private String pythonRetrainUrl;

    public DailyPredictionResponseDto getDailyForecast(long parkingLotId, LocalDate targetDate) {
        List<DailyPredictionEntity> savedPredictions = predictionRepository
                .findByParkingLot_IdAndTargetDateOrderByHourOfDayAsc(parkingLotId, targetDate);

        if (savedPredictions.size() == 24) {
            log.info("[CACHE HIT]: {} numaralı otoparkın {} tarihli tahminleri veritabanından çekildi.", parkingLotId, targetDate);
            List<Integer> ratesFromDb = savedPredictions.stream()
                    .map(entity -> (int) Math.round(entity.getPredictionRate() * 100))
                    .collect(Collectors.toList());

            return DailyPredictionResponseDto.builder()
                    .date(targetDate.toString())
                    .hourlyRates(ratesFromDb)
                    .build();
        }

        log.info("[CACHE MISS]: Veritabanında tahmin yok. Python yapay zekasına istek atılıyor...");

        ParkingLotEntity lot = parkingLotRepository.findById(parkingLotId)
                .orElseThrow(() -> new ResourceNotFoundException("Otopark bulunamadı. ID: " + parkingLotId));

        PredictionRequestDto requestDto = PredictionRequestDto.builder()
                .targetDate(targetDate.toString())
                .isExam(calendarService.isExamWeek(targetDate))
                .isHoliday(calendarService.isHoliday(targetDate))
                .dailyWeather(weatherService.getDailyRainForecast())
                .build();

        try {
            String url = pythonPredictUrl + "/" + parkingLotId;
            PredictionResponseDto response = restTemplate.postForObject(url, requestDto, PredictionResponseDto.class);

            if (response == null || response.getHourlyPredictions() == null
                    || response.getHourlyPredictions().size() != 24) {
                return DailyPredictionResponseDto.builder()
                        .date(targetDate.toString())
                        .hourlyRates(getFallbackRates(parkingLotId, targetDate))
                        .build();
            }

            for (Double rate : response.getHourlyPredictions()) {
                if (rate == null || rate < 0) {
                    return DailyPredictionResponseDto.builder()
                            .date(targetDate.toString())
                            .hourlyRates(getFallbackRates(parkingLotId, targetDate))
                            .build();
                }
            }

            List<Integer> hourlyRates = new ArrayList<>();
            List<DailyPredictionEntity> newEntitiesToSave = new ArrayList<>();

            for (int i = 0; i < response.getHourlyPredictions().size(); i++) {
                Double rate = response.getHourlyPredictions().get(i);
                hourlyRates.add((int) Math.round(rate * 100));

                newEntitiesToSave.add(DailyPredictionEntity.builder()
                        .targetDate(targetDate)
                        .hourOfDay(i)
                        .startTime(LocalTime.of(i, 0))
                        .endTime(LocalTime.of(i, 59, 59))
                        .predictionRate(rate)
                        .predictionAccuracy(0.0)
                        .parkingLot(lot)
                        .build());
            }

            predictionRepository.saveAll(newEntitiesToSave);
            log.info("[DB SAVE]: Python'dan gelen 24 saatlik tahminler veritabanına başarıyla kaydedildi.");

            return DailyPredictionResponseDto.builder()
                    .date(targetDate.toString())
                    .hourlyRates(hourlyRates)
                    .build();

        } catch (Exception e) {
            log.error("[ML BAĞLANTI HATASI]: Python servisine ulaşılamadı. Sebep: {}", e.getMessage());

            return DailyPredictionResponseDto.builder()
                    .date(targetDate.toString())
                    .hourlyRates(getFallbackRates(parkingLotId, targetDate))
                    .build();
        }
    }

    @Scheduled(cron = "0 0 4 * * *")
    public void triggerModelRetraining() {
        try {
            restTemplate.postForObject(pythonRetrainUrl, null, String.class);
            log.info("[ML EĞİTİM]: Gece 04:00 Python model eğitimi başarıyla tetiklendi.");
        } catch (Exception e) {
            log.error("[ML EĞİTİM HATASI]: Eğitim tetiklenemedi. Sebep: {}", e.getMessage());
        }
    }

    private List<Integer> getFallbackRates(long parkingLotId, LocalDate targetDate) {
        LocalDate oneWeekAgo = targetDate.minusDays(7);

        List<DailyPredictionEntity> historicalData = predictionRepository
                .findByParkingLot_IdAndTargetDateOrderByHourOfDayAsc(parkingLotId, oneWeekAgo);

        if (historicalData != null && historicalData.size() == 24) {
            log.warn("[FALLBACK]: Python servisi ulaşılamadı, {} tarihli geçmiş veriler dönülüyor.", oneWeekAgo);
            return historicalData.stream()
                    .map(entity -> (int) Math.round(entity.getPredictionRate() * 100))
                    .collect(Collectors.toList());
        }

        log.error("[CRITICAL]: Python servisi ulaşılamaz durumda ve {} tarihine ait geçmiş veri yok!", oneWeekAgo);
        throw new PythonServiceException("Yapay zeka servisi geçici olarak devre dışı ve referans alınacak geçmiş veri bulunamadı.");
    }
}