package com.parking.parkingadvanced.service;

import com.parking.parkingadvanced.model.AcademicEventEntity;
import com.parking.parkingadvanced.repository.AcademicEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AcademicCalendarService {

    private final AcademicEventRepository calendarRepository;

    private Optional<AcademicEventEntity> getCurrentEvent(LocalDate targetDate) {
        return calendarRepository.
                findByStartDateLessThanEqualAndEndDateGreaterThanEqual(targetDate, targetDate);
    }

    public boolean isExamWeek(LocalDate date) {
        return getCurrentEvent(date).map(AcademicEventEntity::isExamWeek).orElse(false);
    }

    public boolean isHoliday(LocalDate date) {
        return isWeekend(date) || isOfficialHolidayFromDb(date);
    }

    public boolean isTodayExamWeek() {
        return isExamWeek(LocalDate.now());
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    private boolean isOfficialHolidayFromDb(LocalDate date) {
        return getCurrentEvent(date).map(AcademicEventEntity::isHoliday).orElse(false);
    }
}