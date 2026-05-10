package com.parking.parkingadvanced.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE)
public class PredictionRequestDto {

    @JsonProperty("targetDate")
    private String targetDate;

    @JsonProperty("isExam")
    private boolean isExam;

    @JsonProperty("isHoliday")
    private boolean isHoliday;

    @JsonProperty("dailyWeather")
    private List<Boolean> dailyWeather;
}