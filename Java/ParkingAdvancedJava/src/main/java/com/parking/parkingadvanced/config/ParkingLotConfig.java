package com.parking.parkingadvanced.config;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "parking")
public class ParkingLotConfig {
    private List<LotData> lots;

    @Data
    public static class LotData {
        private long id;
        private String name;
        private int capacity;
    }
}
