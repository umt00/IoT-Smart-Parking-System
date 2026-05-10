package com.parking.parkingadvanced.service;

import com.parking.parkingadvanced.dto.ParkingLotDto;
import com.parking.parkingadvanced.dto.ParkingStatusResponseDto;
import com.parking.parkingadvanced.exception.ResourceNotFoundException;
import com.parking.parkingadvanced.model.ParkingLotEntity;
import com.parking.parkingadvanced.model.ParkingTransactionEntity;
import com.parking.parkingadvanced.repository.ParkingLotRepository;
import com.parking.parkingadvanced.repository.ParkingTransactionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class ParkingTransactionService {

    private final ParkingTransactionalRepository parkingTransactionalRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final WeatherService weatherService;
    private final AcademicCalendarService calendarService;

    @Transactional
    public void processAction(long parkingLotId, boolean isEntry) {

        ParkingLotEntity lot = parkingLotRepository.findByIdLocked(parkingLotId)
                .orElseThrow(() -> new ResourceNotFoundException(parkingLotId + " numaralı otopark sistemde bulunamadı."));

        int currentCount = lot.getCurrentCount();
        int newCount = isEntry ? (currentCount + 1) : Math.max(0, currentCount - 1);

        lot.setCurrentCount(newCount);

        ParkingTransactionEntity newRecord = ParkingTransactionEntity.builder()
                .currentCount(newCount)
                .isEntry(isEntry)
                .isRaining(weatherService.isRainingNow())
                .isHoliday(calendarService.isHoliday(LocalDate.now()))
                .isExamWeek(calendarService.isTodayExamWeek())
                .eventTime(LocalDateTime.now())
                .parkingLot(lot)
                .isOverflow(newCount > lot.getCapacity())
                .build();

        parkingTransactionalRepository.save(newRecord);
        parkingLotRepository.save(lot);
    }

    public ParkingStatusResponseDto getParkingStatus(long parkingLotId) {

        ParkingLotEntity lot = parkingLotRepository.findById(parkingLotId)
                .orElseThrow(() -> new ResourceNotFoundException(parkingLotId + " numaralı otopark sistemde bulunamadı."));

        double currentRate = (double) lot.getCurrentCount() / lot.getCapacity();
        int percentage = (int) Math.round(currentRate * 100);

        return ParkingStatusResponseDto.builder()
                .parkName(lot.getParkName())
                .occupancyPercentage(percentage)
                .currentCount(lot.getCurrentCount())
                .build();
    }

    public ParkingLotDto getBestParkingLot() {
        return parkingLotRepository.findAll().stream()
                .max(Comparator.comparingInt(lot -> lot.getCapacity() - lot.getCurrentCount()))
                .map(lot -> ParkingLotDto.builder()
                        .id(lot.getId())
                        .name(lot.getParkName())
                        .remaining(lot.getCapacity()-lot.getCurrentCount())
                        .build())
                .orElseThrow(() -> new ResourceNotFoundException("Sistemde aktif otopark bulunamadı."));
    }
}