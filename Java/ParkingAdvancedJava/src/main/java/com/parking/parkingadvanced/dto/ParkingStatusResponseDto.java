package com.parking.parkingadvanced.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingStatusResponseDto {
    private String parkName;
    private int currentCount;
    private int occupancyPercentage;
}