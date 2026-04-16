package com.parking.parkingdemov2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestDto {
    private String targetDate;
    private boolean isExamWeek;
    private boolean isHoliday;
    private List<Boolean> hourlyWeather;
}