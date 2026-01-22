package com.pinapp.gateway.infrastructure.notification;

import com.pinapp.gateway.domain.model.NotificationStatus;
import com.pinapp.gateway.domain.model.TransactionStatusInfo;
import com.pinapp.gateway.domain.ports.out.TransactionStatusPort;
import com.pinapp.notify.core.events.NotificationEvent;
import com.pinapp.notify.core.events.NotificationFailedEvent;
import com.pinapp.notify.core.events.NotificationSentEvent;
import com.pinapp.notify.core.events.NotificationSubscriber;
import com.pinapp.notify.domain.NotificationResult;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TransactionAuditListener implements NotificationSubscriber {

    private final TransactionStatusPort statusPort;

    public TransactionAuditListener(TransactionStatusPort statusPort) {
        this.statusPort = statusPort;
    }

    @Override
    public void onEvent(NotificationEvent event) {
        switch (event) {
            case NotificationSentEvent sent -> {
                NotificationResult result = NotificationResult.success(
                        UUID.fromString(sent.notificationId()),
                        sent.provider(),
                        sent.channel());
                System.out.println("[AUDIT-SUCCESS] Notification sent: " + result);
                updateStatusStore(result);
            }
            case NotificationFailedEvent failed -> {
                NotificationResult result = NotificationResult.failure(
                        UUID.fromString(failed.notificationId()),
                        failed.provider(),
                        failed.channel(),
                        failed.errorMessage());
                System.out.println("[AUDIT-FAILURE] Notification failed: " + result);
                updateStatusStore(result);
            }
            default -> System.out.println("[AUDIT-UNKNOWN] Unknown event type: " + event.getClass().getName());
        }
    }

    private void updateStatusStore(NotificationResult result) {
        NotificationStatus domainNotificationStatus = new NotificationStatus(
                result.success(),
                result.notificationId().toString(),
                result.providerName(),
                result.errorMessage());

        TransactionStatusInfo statusInfo = new TransactionStatusInfo(
                result.notificationId().toString(),
                result.success() ? "COMPLETED" : "FAILED",
                domainNotificationStatus);

        statusPort.save(statusInfo);
    }
}
