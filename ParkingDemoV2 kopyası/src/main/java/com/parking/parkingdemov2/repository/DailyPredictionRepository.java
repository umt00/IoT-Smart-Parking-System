package com.parking.parkingdemov2.repository;

import com.parking.parkingdemov2.model.DailyPredictionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyPredictionRepository extends JpaRepository<DailyPredictionEntity, Long> {

    List<DailyPredictionEntity> findByTargetDateOrderByHourOfDayAsc(LocalDate targetDate);

    @Modifying
    @Query("DELETE FROM DailyPredictionEntity d WHERE d.targetDate = :targetDate")
    void deleteByTargetDate(@Param("targetDate") LocalDate targetDate);
}