package com.parking.parkingadvanced.repository;

import com.parking.parkingadvanced.model.ParkingLotEntity;
import com.parking.parkingadvanced.model.ParkingTransactionEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ParkingTransactionalRepositoryTest {

    @Autowired
    private ParkingTransactionalRepository transactionRepository;

    @Autowired
    private ParkingLotRepository lotRepository;

    @Test
    void saveAndFindTopByParkingLotOrderByIdDesc_ShouldReturnLatestTransaction() {
        ParkingLotEntity lot = ParkingLotEntity.builder()
                .id(1L)
                .parkName("Haliç Merkez Otopark")
                .capacity(100)
                .build();

        lotRepository.save(lot);

        ParkingTransactionEntity tx1 = ParkingTransactionEntity.builder()
                .parkingLot(lot)
                .currentCount(50)
                .isEntry(true)
                .eventTime(LocalDateTime.now().minusMinutes(10))
                .build();

        transactionRepository.save(tx1);

        ParkingTransactionEntity tx2 = ParkingTransactionEntity.builder()
                .parkingLot(lot)
                .currentCount(60)
                .isEntry(true)
                .eventTime(LocalDateTime.now())
                .build();

        transactionRepository.save(tx2);

        Optional<ParkingTransactionEntity> latestTx = transactionRepository.findTopByParkingLotOrderByIdDesc(lot);

        assertTrue(latestTx.isPresent());
        double occupancyRate = (double) latestTx.get().getCurrentCount() / lot.getCapacity();
        assertEquals(0.6, occupancyRate);
    }
}