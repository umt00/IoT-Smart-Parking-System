package com.parking.parkingadvanced.service;

import com.parking.parkingadvanced.model.ParkingLotEntity;
import com.parking.parkingadvanced.model.ParkingTransactionEntity;
import com.parking.parkingadvanced.repository.ParkingLotRepository;
import com.parking.parkingadvanced.repository.ParkingTransactionalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParkingTransactionServiceTest {

    @Mock
    private ParkingLotRepository parkingLotRepository;

    @Mock
    private ParkingTransactionalRepository parkingTransactionalRepository;

    @Mock
    private WeatherService weatherService;

    @Mock
    private AcademicCalendarService calendarService;

    @InjectMocks
    private ParkingTransactionService transactionService;

    @Test
    void processAction_WhenEmptyAndExit_ShouldStayAtZero() {
        long lotId = 1L;
        ParkingLotEntity mockLot = ParkingLotEntity.builder()
                .id(lotId)
                .capacity(100)
                .currentCount(0)
                .build();

        when(parkingLotRepository.findByIdLocked(lotId)).thenReturn(Optional.of(mockLot));
        when(weatherService.isRainingNow()).thenReturn(false);
        when(calendarService.isHoliday(any())).thenReturn(false);
        when(calendarService.isTodayExamWeek()).thenReturn(false);

        transactionService.processAction(lotId, false);

        ArgumentCaptor<ParkingTransactionEntity> captor = ArgumentCaptor.forClass(ParkingTransactionEntity.class);
        verify(parkingTransactionalRepository).save(captor.capture());

        assertEquals(0, captor.getValue().getCurrentCount());
        assertEquals(0.0, (double) captor.getValue().getCurrentCount() / mockLot.getCapacity());
    }

    @Test
    void processAction_WhenFullAndEntry_ShouldSetOverflowTrue() {
        long lotId = 1L;
        ParkingLotEntity mockLot = ParkingLotEntity.builder()
                .id(lotId)
                .capacity(100)
                .currentCount(100)
                .build();

        when(parkingLotRepository.findByIdLocked(lotId)).thenReturn(Optional.of(mockLot));
        when(weatherService.isRainingNow()).thenReturn(false);
        when(calendarService.isHoliday(any())).thenReturn(false);
        when(calendarService.isTodayExamWeek()).thenReturn(false);

        transactionService.processAction(lotId, true);

        ArgumentCaptor<ParkingTransactionEntity> captor = ArgumentCaptor.forClass(ParkingTransactionEntity.class);
        verify(parkingTransactionalRepository).save(captor.capture());

        assertTrue(captor.getValue().isOverflow());
        assertEquals(101, captor.getValue().getCurrentCount());
        assertEquals(1.01, (double) captor.getValue().getCurrentCount() / mockLot.getCapacity());
    }
}