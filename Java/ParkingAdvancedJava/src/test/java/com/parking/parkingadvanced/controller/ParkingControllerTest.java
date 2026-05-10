package com.parking.parkingadvanced.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parking.parkingadvanced.dto.SensorRequestDto;
import com.parking.parkingadvanced.service.ParkingTransactionService;
import com.parking.parkingadvanced.service.PredictionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ParkingController.class)
class ParkingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ParkingTransactionService transactionService;

    @MockBean
    private PredictionService predictionService;

    @Test
    void handleSensorAction_WhenInvalidLotId_ShouldReturn400BadRequest() throws Exception {
        SensorRequestDto badRequest = SensorRequestDto.builder()
                .parkingLotId(-5L)
                .isEntry(true)
                .build();

        String jsonPayload = objectMapper.writeValueAsString(badRequest);

        mockMvc.perform(post("/api/parking/sensor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void handleSensorAction_WhenNullLotId_ShouldReturn400BadRequest() throws Exception {
        SensorRequestDto emptyRequest = new SensorRequestDto();
        emptyRequest.setIsEntry(false);

        String jsonPayload = objectMapper.writeValueAsString(emptyRequest);

        mockMvc.perform(post("/api/parking/sensor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest());
    }
}