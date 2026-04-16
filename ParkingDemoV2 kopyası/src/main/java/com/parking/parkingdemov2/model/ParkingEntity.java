package com.parking.parkingdemov2.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParkingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private double occupancyRate;
    private int totalCapacity;
    private boolean isEntry;
    private boolean isRaining;
    private boolean isHoliday;
    private boolean isExamWeek;
    private boolean isOverflow;
    private LocalDateTime eventTime;
}