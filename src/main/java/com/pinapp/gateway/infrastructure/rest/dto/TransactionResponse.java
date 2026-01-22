package com.pinapp.gateway.infrastructure.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        BigDecimal amount,
        String status,
        NotificationSummaryResponse notification) {
}
