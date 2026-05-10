package com.parking.parkingadvanced.service;

import com.parking.parkingadvanced.model.AcademicEventEntity;
import com.parking.parkingadvanced.repository.AcademicEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcademicCalendarServiceTest {

    @Mock
    private AcademicEventRepository calendarRepository;

    @InjectMocks
    private AcademicCalendarService calendarService;

    @Test
    void isHoliday_WhenWeekend_ShouldReturnTrue() {
        LocalDate saturday = LocalDate.of(2026, 5, 2);
        boolean result = calendarService.isHoliday(saturday);
        assertTrue(result);
    }

    @Test
    void isExamWeek_WhenEventExistsInDb_ShouldReturnTrue() {
        LocalDate weekday = LocalDate.of(2026, 5, 6);

        AcademicEventEntity mockEvent = AcademicEventEntity.builder()
                .eventName("Midterm")
                .examWeek(true)
                .build();

        when(calendarRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(weekday, weekday))
                .thenReturn(Optional.of(mockEvent));

        boolean result = calendarService.isExamWeek(weekday);
        assertTrue(result);
    }
}