package com.parking.parkingdemov2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HourlyWeatherDto {
    private int hour;
    @JsonProperty("is_raining")
    private boolean isRaining;
}