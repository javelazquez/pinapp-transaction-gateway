package com.pinapp.gateway.domain.model;

public record ProcessingResult(
        Transaction transaction,
        NotificationStatus notificationStatus) {
}
