package com.parking.parkingdemov2.service;

import com.parking.parkingdemov2.dto.DailyPredictionResponse;
import com.parking.parkingdemov2.dto.PredictionResponse;
import com.parking.parkingdemov2.dto.RequestDto;
import com.parking.parkingdemov2.model.AcademicEvent;
import com.parking.parkingdemov2.model.DailyPredictionEntity;
import com.parking.parkingdemov2.model.ParkingEntity;
import com.parking.parkingdemov2.repository.AcademicCalendarRepository;
import com.parking.parkingdemov2.repository.DailyPredictionRepository;
import com.parking.parkingdemov2.repository.ParkingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParkingService {

    private final WeatherService weatherService;
    private final ParkingRepository parkingRepository;
    private final AcademicCalendarRepository calendarRepository;
    private final DailyPredictionRepository predictionRepository;
    private final RestTemplate restTemplate;

    @Value("${parking.total-capacity}")
    private int totalCapacity;

    @Value("${python.api.url}")
    private String pythonApiUrl;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void processAction(boolean isEntry) {
        ParkingEntity lastRecord = parkingRepository.findTopByOrderByIdDesc();

        double currentRate = (lastRecord == null) ? 0.0 : lastRecord.getOccupancyRate();
        double impact = 1.0 / totalCapacity;

        double newRate = isEntry ? currentRate + impact : Math.max(0.0, currentRate - impact);
        boolean isOverflow = newRate > 1.0;

        LocalDateTime now = LocalDateTime.now();
        Optional<AcademicEvent> currentEvent = calendarRepository.findEventByDate(now.toLocalDate());

        boolean isExam = currentEvent.map(AcademicEvent::isExamWeek).orElse(false);

        ParkingEntity newEntity = new ParkingEntity(
                null, newRate, totalCapacity, isEntry,
                weatherService.isRainingNow(), checkIfWeekend(now), isExam, isOverflow, now
        );

        parkingRepository.save(newEntity);
    }

    public Map<String, Object> getCurrentStatus() {
        ParkingEntity lastRecord = parkingRepository.findTopByOrderByIdDesc();
        double rate = (lastRecord == null) ? 0.0 : lastRecord.getOccupancyRate();

        Map<String, Object> status = new HashMap<>();
        status.put("occupancyPercentage", (int) Math.round(rate * 100));
        status.put("isOverflow", rate > 1.0);
        return status;
    }

    @Transactional
    @Scheduled(cron = "0 0 3 * * *")
    @EventListener(ApplicationReadyEvent.class)
    public void fetchAndSavePredictions() {
        LocalDate targetDate = LocalDate.now();
        log.info("[JAVA]: Python API'ye tahmin istegi atiliyor...");

        try {
            Optional<AcademicEvent> currentEvent = calendarRepository.findEventByDate(targetDate);
            boolean isExam = currentEvent.map(AcademicEvent::isExamWeek).orElse(false);
            boolean isHoliday = checkIfWeekend(targetDate.atStartOfDay());

            List<Boolean> dailyWeather = weatherService.getDailyRainForecast();

            RequestDto requestDto = new RequestDto(targetDate.toString(), isExam, isHoliday, dailyWeather);
            PredictionResponse response = restTemplate.postForObject(
                    pythonApiUrl + "/predict/daily-batch", requestDto, PredictionResponse.class
            );

            if (response != null && response.getHourlyPredictions() != null) {
                log.info("[JAVA]: Tahminler basariyla alindi, veritabanina yaziliyor.");
                savePredictions(targetDate, response.getHourlyPredictions(), true);
            } else {
                log.warn("[JAVA]: Python API bos dondu, fallback tetikleniyor.");
                triggerFallback(targetDate);
            }
        } catch (Exception e) {
            log.error("[JAVA HATA]: Python API baglanti hatasi: {}", e.getMessage());
            triggerFallback(targetDate);
        }
    }

    private void triggerFallback(LocalDate targetDate) {
        for (int i = 7; i <= 21; i += 7) {
            LocalDate fallbackDate = targetDate.minusDays(i);
            List<DailyPredictionEntity> data = predictionRepository.findByTargetDateOrderByHourOfDayAsc(fallbackDate);

            if (!data.isEmpty()) {
                log.info("[JAVA]: {} gun oncesinin verisi ile fallback basarili.", i);
                List<Double> predictions = data.stream()
                        .map(DailyPredictionEntity::getPredictedRate)
                        .collect(Collectors.toList());

                savePredictions(targetDate, predictions, false);
                return;
            }
        }
        log.error("[JAVA]: 21 gun geriye donuk veri bulunamadi, tahmin grafigi bos kalacak!");
    }

    @Transactional
    public void savePredictions(LocalDate targetDate, List<Double> rates, boolean isAccurate) {
        predictionRepository.deleteByTargetDate(targetDate);

        for (int i = 0; i < rates.size(); i++) {
            predictionRepository.save(
                    new DailyPredictionEntity(null, targetDate, i, rates.get(i), isAccurate)
            );
        }
    }

    public DailyPredictionResponse getPredictionsForFrontend(LocalDate date) {
        List<DailyPredictionEntity> entities = predictionRepository.findByTargetDateOrderByHourOfDayAsc(date);

        List<Integer> formattedRates = entities.stream()
                .map(e -> (int) Math.min(100, Math.round(e.getPredictedRate() * 100)))
                .collect(Collectors.toList());

        return new DailyPredictionResponse(date, formattedRates);
    }

    private boolean checkIfWeekend(LocalDateTime time) {
        var day = time.getDayOfWeek();
        return day == java.time.DayOfWeek.SATURDAY || day == java.time.DayOfWeek.SUNDAY;
    }

    @Scheduled(cron = "0 0 4 * * SUN")
    public void triggerModelRetraining() {
        log.info("[JAVA]: Python modelini yeniden egitme (Retrain) istegi baslatiliyor...");
        try {
            String retrainUrl = pythonApiUrl + "/train/retrain";
            Map<String, String> response = restTemplate.postForObject(retrainUrl, null, Map.class);

            if (response != null && "success".equals(response.get("status"))) {
                log.info("[JAVA]: Model basariyla egitildi. Detay: {}", response.get("message"));
            } else {
                log.warn("[JAVA]: Model egitimi basarisiz oldu veya beklenmeyen yanit geldi.");
            }
        } catch (Exception e) {
            log.error("[JAVA HATA]: Model yeniden egitim baglantisinda sorun: {}", e.getMessage());
        }
    }
}