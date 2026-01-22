package com.pinapp.gateway.infrastructure.rest.dto;

public record TransactionStatusDTO(
        String id,
        String status,
        NotificationSummaryResponse result) {
}
