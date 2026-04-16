package com.parking.parkingdemov2.repository;

import com.parking.parkingdemov2.model.AcademicEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface AcademicCalendarRepository extends JpaRepository<AcademicEvent, Long> {

    @Query("SELECT a FROM AcademicEvent a WHERE :date BETWEEN a.startDate AND a.endDate")
    Optional<AcademicEvent> findEventByDate(@Param("date") LocalDate date);
}