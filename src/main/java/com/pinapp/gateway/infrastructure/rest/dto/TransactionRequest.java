package com.pinapp.gateway.infrastructure.rest.dto;

import com.pinapp.gateway.domain.model.TransactionStatus;
import java.math.BigDecimal;
import java.util.UUID;

public record TransactionRequest(
        UUID id,
        BigDecimal amount,
        String customerName,
        String email,
        String phone,
        TransactionStatus status) {
}
