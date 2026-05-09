package com.example.backend.health.dto;

import java.time.LocalDateTime;

public record HealthResponse(
        String status,
        String message,
        LocalDateTime timestamp
) {
    public static HealthResponse healthy() {
        return new HealthResponse(
                "UP",
                "Service is healthy",
                LocalDateTime.now()
        );
    }
}
