package com.parking.parkingadvanced.repository;

import com.parking.parkingadvanced.model.ParkingLotEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class ParkingLotRepositoryTest {

    @Autowired
    private ParkingLotRepository repository;

    @Test
    void findByIdLocked_ShouldExecuteSqlAndReturnEntity() {
        ParkingLotEntity lot = ParkingLotEntity.builder()
                .id(1L)
                .parkName("Haliç Merkez Otopark")
                .capacity(150)
                .currentCount(50)
                .build();

        repository.save(lot);

        Optional<ParkingLotEntity> foundLot = repository.findByIdLocked(1L);

        assertTrue(foundLot.isPresent());
        assertEquals("Haliç Merkez Otopark", foundLot.get().getParkName());
    }
}