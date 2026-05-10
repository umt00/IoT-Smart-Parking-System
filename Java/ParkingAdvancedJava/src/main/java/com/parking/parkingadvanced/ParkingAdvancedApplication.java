package com.parking.parkingadvanced;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ParkingAdvancedApplication {

    public static void main(String[] args) {
        SpringApplication.run(ParkingAdvancedApplication.class, args);
    }

}
