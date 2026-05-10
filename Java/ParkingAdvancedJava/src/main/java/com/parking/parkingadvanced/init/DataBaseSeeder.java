package com.parking.parkingadvanced.init;

import com.parking.parkingadvanced.config.ParkingLotConfig;
import com.parking.parkingadvanced.model.ParkingLotEntity;
import com.parking.parkingadvanced.repository.ParkingLotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataBaseSeeder {

    private final ParkingLotRepository parkingLotRepository;
    private final ParkingLotConfig parkingLotConfig;

    @PostConstruct
    public void seedParkingLots() {
        List<ParkingLotConfig.LotData> lots = parkingLotConfig.getLots();

        if (lots != null && !lots.isEmpty()) {
            for (ParkingLotConfig.LotData lotData : lots) {
                long parkingLotId = lotData.getId();

                // Upsert: Eğer varsa güncelle yoksa ekle
                var existing = parkingLotRepository.findById(parkingLotId);
                if (existing.isPresent()) {
                    ParkingLotEntity entity = existing.get();
                    entity.setParkName(lotData.getName());
                    entity.setCapacity(lotData.getCapacity());
                    parkingLotRepository.save(entity);
                    log.info("[SEEDER]: Otopark güncellendi - ID: {}, İsim: {}, Kapasite: {}",
                            parkingLotId, lotData.getName(), lotData.getCapacity());
                } else {
                    ParkingLotEntity entity = ParkingLotEntity.builder()
                            .id(lotData.getId())
                            .parkName(lotData.getName())
                            .capacity(lotData.getCapacity())
                            .build();
                    parkingLotRepository.save(entity);
                    log.info("[SEEDER]: Otopark eklendi - ID: {}, İsim: {}, Kapasite: {}",
                            entity.getId(), lotData.getName(), lotData.getCapacity());
                }
            }
            log.info("[SEEDER]: Toplam {} otopark başarıyla işlendi.", lots.size());
        } else {
            log.warn("[SEEDER]: application.properties'de otopark listesi bulunamadı!");
        }
    }
}
