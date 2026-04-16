package com.parking.parkingdemov2.repository;

import com.parking.parkingdemov2.model.ParkingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParkingRepository extends JpaRepository<ParkingEntity, Long> {

    ParkingEntity findTopByOrderByIdDesc();
}