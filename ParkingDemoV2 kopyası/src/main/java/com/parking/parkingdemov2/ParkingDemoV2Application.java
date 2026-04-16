package com.parking.parkingdemov2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ParkingDemoV2Application {

    public static void main(String[] args) {
        SpringApplication.run(ParkingDemoV2Application.class, args);
    }

}
