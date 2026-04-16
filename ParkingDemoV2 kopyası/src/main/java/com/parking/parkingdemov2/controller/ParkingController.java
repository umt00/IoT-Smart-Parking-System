package com.parking.parkingdemov2.controller;

import com.parking.parkingdemov2.dto.DailyPredictionResponse;
import com.parking.parkingdemov2.service.ParkingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/parking")
@RequiredArgsConstructor
public class ParkingController {

    private final ParkingService parkingService;

    @PostMapping("/entry")
    public ResponseEntity<String> recordEntry() {
        log.info("[API]: Arac giris tetiklendi.");
        parkingService.processAction(true);
        return ResponseEntity.ok("Entry recorded successfully.");
    }

    @PostMapping("/exit")
    public ResponseEntity<String> recordExit() {
        log.info("[API]: Arac cikis tetiklendi.");
        parkingService.processAction(false);
        return ResponseEntity.ok("Exit recorded successfully.");
    }

    @GetMapping("/predictions")
    public ResponseEntity<DailyPredictionResponse> getPredictions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        log.info("[API]: Tahmin verileri istendi. Tarih: {}", targetDate);
        return ResponseEntity.ok(parkingService.getPredictionsForFrontend(targetDate));
    }

    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentStatus() {
        return ResponseEntity.ok(parkingService.getCurrentStatus());
    }
}