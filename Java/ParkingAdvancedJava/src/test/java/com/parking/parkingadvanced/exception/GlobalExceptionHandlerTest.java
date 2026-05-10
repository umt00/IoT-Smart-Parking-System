package com.parking.parkingadvanced.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    void handleNotFoundException_ShouldReturn404AndCorrectJsonFormat() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Otopark sistemde bulunamadı.");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Not Found", response.getBody().get("error"));
        assertEquals("Otopark sistemde bulunamadı.", response.getBody().get("message"));
    }

    @Test
    void handlePythonException_ShouldReturn503AndCorrectJsonFormat() {
        PythonServiceException ex = new PythonServiceException("ML servisi çöktü, fallback aktif.");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handlePythonException(ex);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("Service Unavailable", response.getBody().get("error"));
        assertEquals("ML servisi çöktü, fallback aktif.", response.getBody().get("message"));
    }
}