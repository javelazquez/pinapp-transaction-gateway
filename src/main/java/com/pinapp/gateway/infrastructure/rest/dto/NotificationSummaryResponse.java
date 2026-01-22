package com.pinapp.gateway.infrastructure.rest.dto;

public record NotificationSummaryResponse(
        Boolean success,
        String messageId,
        String provider,
        String errorMessage) {
}
