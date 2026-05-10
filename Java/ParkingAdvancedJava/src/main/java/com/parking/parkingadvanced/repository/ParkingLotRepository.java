package com.parking.parkingadvanced.repository;


import com.parking.parkingadvanced.model.ParkingLotEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParkingLotRepository extends JpaRepository<ParkingLotEntity, Long> {

    // Bu kilit sayesinde aynı anda 2 sensör bu otoparkı çekemez, biri bekler!
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ParkingLotEntity p WHERE p.id = :id")
    Optional<ParkingLotEntity> findByIdLocked(Long id);
}
