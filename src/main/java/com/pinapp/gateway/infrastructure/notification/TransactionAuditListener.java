package com.pinapp.gateway.infrastructure.notification;

import com.pinapp.notify.core.events.NotificationEvent;
import com.pinapp.notify.core.events.NotificationFailedEvent;
import com.pinapp.notify.core.events.NotificationSentEvent;
import com.pinapp.notify.core.events.NotificationSubscriber;
import com.pinapp.notify.domain.NotificationResult;
import org.springframework.stereotype.Component;

import com.pinapp.gateway.infrastructure.rest.dto.NotificationSummaryResponse;
import com.pinapp.gateway.infrastructure.rest.dto.TransactionStatusDTO;
import com.pinapp.gateway.infrastructure.store.NotificationStatusStore;
import java.util.UUID;

@Component
public class TransactionAuditListener implements NotificationSubscriber {

    private final NotificationStatusStore statusStore;

    public TransactionAuditListener(NotificationStatusStore statusStore) {
        this.statusStore = statusStore;
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
        TransactionStatusDTO statusDTO = new TransactionStatusDTO(
                result.notificationId().toString(),
                result.success() ? "COMPLETED" : "FAILED",
                new NotificationSummaryResponse(
                        result.success(),
                        result.notificationId().toString(),
                        result.providerName(),
                        result.errorMessage()));
        statusStore.updateStatus(result.notificationId().toString(), statusDTO);
    }
}
