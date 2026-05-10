package com.parking.parkingadvanced.controller;

import com.parking.parkingadvanced.dto.DailyPredictionResponseDto;
import com.parking.parkingadvanced.dto.ParkingLotDto;
import com.parking.parkingadvanced.dto.ParkingStatusResponseDto;
import com.parking.parkingadvanced.dto.SensorRequestDto;
import com.parking.parkingadvanced.service.ParkingTransactionService;
import com.parking.parkingadvanced.service.PredictionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/parking")
@RequiredArgsConstructor
public class ParkingController {

    private final ParkingTransactionService transactionService;
    private final PredictionService predictionService;

    @PostMapping("/sensor")
    public ResponseEntity<String> handleSensorAction(@Valid @RequestBody SensorRequestDto request) {
        log.info("[API]: Araç hareketi algılandı. Otopark: {}, Giriş: {}",
                request.getParkingLotId(), request.getIsEntry());

        transactionService.processAction(request.getParkingLotId(), request.getIsEntry());
        return ResponseEntity.ok("İşlem başarıyla kaydedildi.");
    }

    @GetMapping("/recommended")
    public ResponseEntity<ParkingLotDto> getRecommendedParkingLot() {
        log.info("[API]: En uygun otopark tavsiyesi istendi.");
        return ResponseEntity.ok(transactionService.getBestParkingLot());
    }

//    @GetMapping("/metrics")
//    public ResponseEntity<String> getMetrics() {
//        log.info("[API]: Prometheus metrikleri istendi.");
//        return ResponseEntity.ok(transactionService.getPrometheusMetrics());
//    }

    @GetMapping("/current/{parkingLotId}")
    public ResponseEntity<ParkingStatusResponseDto> getCurrentStatus(@PathVariable long parkingLotId) {
        log.info("[API]: Anlık doluluk istendi. Otopark ID: {}", parkingLotId);
        return ResponseEntity.ok(transactionService.getParkingStatus(parkingLotId));
    }

    @GetMapping("/predictions/{parkingLotId}")
    public ResponseEntity<DailyPredictionResponseDto> getDailyPredictions(
            @PathVariable long parkingLotId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        log.info("[API]: Tahminler istendi. Otopark: {}, Tarih: {}", parkingLotId, targetDate);

        DailyPredictionResponseDto response = predictionService.getDailyForecast(parkingLotId, targetDate);

        return ResponseEntity.ok(response);
    }
}