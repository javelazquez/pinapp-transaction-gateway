package com.pinapp.gateway.domain.model;

/**
 * Domain record representing the status of a transaction and its notification.
 */
public record TransactionStatusInfo(
        String id,
        String status,
        NotificationStatus notificationStatus) {
}
