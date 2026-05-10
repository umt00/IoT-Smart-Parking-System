package com.parking.parkingadvanced.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensorRequestDto {

    @NotNull(message = "Otopark ID boş olamaz.")
    @Min(value = 1, message = "Otopark ID geçersiz.")
    private Long parkingLotId;

    private Boolean isEntry;
}