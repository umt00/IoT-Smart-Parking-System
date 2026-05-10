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
public class WeatherResponse {

    private HourlyData hourly;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HourlyData {
        private List<Double> rain;
    }
}
