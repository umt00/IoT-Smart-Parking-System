package com.parking.parkingadvanced.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ParkingTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private int currentCount;
    private boolean isRaining;
    private boolean isHoliday;
    private boolean isExamWeek;
    private boolean isOverflow;

    private LocalDateTime eventTime;

    @NotNull
    private Boolean isEntry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_lot_id")
    private ParkingLotEntity parkingLot;
}
