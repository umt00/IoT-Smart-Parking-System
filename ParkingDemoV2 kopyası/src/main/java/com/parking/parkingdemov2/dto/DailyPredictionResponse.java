package com.parking.parkingdemov2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyPredictionResponse {
    private LocalDate targetDate;
    private List<Integer> hourlyPredictions;
}