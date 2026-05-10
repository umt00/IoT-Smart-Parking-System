package com.parking.parkingadvanced.repository;

import com.parking.parkingadvanced.model.DailyPredictionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyPredictionRepository extends JpaRepository<DailyPredictionEntity, Long> {

    List<DailyPredictionEntity> findByParkingLot_IdAndTargetDateOrderByHourOfDayAsc(Long parkingLotId, LocalDate targetDate);
}