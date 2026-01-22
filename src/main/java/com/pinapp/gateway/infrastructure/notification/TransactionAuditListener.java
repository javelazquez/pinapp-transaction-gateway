package com.pinapp.gateway.infrastructure.notification;

import com.pinapp.notify.core.events.NotificationEvent;
import com.pinapp.notify.core.events.NotificationFailedEvent;
import com.pinapp.notify.core.events.NotificationSentEvent;
import com.pinapp.notify.core.events.NotificationSubscriber;
import com.pinapp.notify.domain.NotificationResult;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TransactionAuditListener implements NotificationSubscriber {

    @Override
    public void onEvent(NotificationEvent event) {
        switch (event) {
            case NotificationSentEvent sent -> {
                NotificationResult result = NotificationResult.success(
                        UUID.fromString(sent.notificationId()),
                        sent.provider(),
                        sent.channel());
                System.out.println("[AUDIT-SUCCESS] Notification sent: " + result);
            }
            case NotificationFailedEvent failed -> {
                NotificationResult result = NotificationResult.failure(
                        UUID.fromString(failed.notificationId()),
                        failed.provider(),
                        failed.channel(),
                        failed.errorMessage());
                System.out.println("[AUDIT-FAILURE] Notification failed: " + result);
            }
            default -> System.out.println("[AUDIT-UNKNOWN] Unknown event type: " + event.getClass().getName());
        }
    }
}
