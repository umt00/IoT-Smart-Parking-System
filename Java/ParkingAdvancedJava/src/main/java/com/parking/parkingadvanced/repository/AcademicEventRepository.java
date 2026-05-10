package com.parking.parkingadvanced.repository;

import com.parking.parkingadvanced.model.AcademicEventEntity;
import com.parking.parkingadvanced.model.ParkingLotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface AcademicEventRepository extends JpaRepository<AcademicEventEntity, Long> {

    Optional<AcademicEventEntity> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate date1, LocalDate date2);
}
