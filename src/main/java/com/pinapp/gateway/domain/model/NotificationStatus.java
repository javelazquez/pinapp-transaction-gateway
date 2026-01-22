package com.pinapp.gateway.domain.model;

public record NotificationStatus(
        boolean success,
        String messageId,
        String provider,
        String errorMessage) {
}
