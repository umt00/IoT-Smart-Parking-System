package com.parking.parkingadvanced.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyPredictionResponseDto {
    private String date;
    private List<Integer> hourlyRates;
}