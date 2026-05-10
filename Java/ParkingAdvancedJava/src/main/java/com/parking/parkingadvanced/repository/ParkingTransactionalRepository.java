package com.parking.parkingadvanced.repository;

import com.parking.parkingadvanced.model.ParkingLotEntity;
import com.parking.parkingadvanced.model.ParkingTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParkingTransactionalRepository extends JpaRepository<ParkingTransactionEntity, Long> {
    Optional<ParkingTransactionEntity> findTopByParkingLotOrderByIdDesc(ParkingLotEntity parkingLotEntity);
}
