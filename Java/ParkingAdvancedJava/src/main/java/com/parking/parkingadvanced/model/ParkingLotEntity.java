package com.parking.parkingadvanced.model;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ParkingLotEntity {

    @Id
    private long  id;
    private String parkName;
    private int capacity;

    private int currentCount;

}
