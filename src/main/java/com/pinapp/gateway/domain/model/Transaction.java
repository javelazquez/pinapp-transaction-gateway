package com.pinapp.gateway.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public record Transaction(
    UUID id,
    BigDecimal amount,
    String customerName,
    String email,
    String phone,
    TransactionStatus status,
    String deviceToken
) {}
