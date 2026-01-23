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
        // Log detallado al inicio para diagnosticar qué eventos se reciben
        String eventType = event.getClass().getSimpleName();
        String notificationId = extractNotificationId(event);
        System.out.println("[AUDIT] Received event: " + eventType + " for notificationId: " + notificationId);
        
        switch (event) {
            case NotificationSentEvent sent -> {
                NotificationResult result = NotificationResult.success(
                        UUID.fromString(sent.notificationId()),
                        sent.provider(),
                        sent.channel());
                System.out.println("[AUDIT-SUCCESS] Notification sent. ID: " + sent.notificationId() + 
                        ", Provider: " + sent.provider() + ", Channel: " + sent.channel());
                updateStatusStore(result);
            }
            case NotificationFailedEvent failed -> {
                String errorMessage = failed.errorMessage();
                System.out.println("[AUDIT-FAILURE] Notification failed. ID: " + failed.notificationId() + 
                        ", Provider: " + failed.provider() + ", Channel: " + failed.channel() + 
                        ", Error: " + (errorMessage != null ? errorMessage : "No error message"));
                
                NotificationResult result = NotificationResult.failure(
                        UUID.fromString(failed.notificationId()),
                        failed.provider(),
                        failed.channel(),
                        errorMessage);
                updateStatusStore(result);
            }
            default -> System.out.println("[AUDIT-UNKNOWN] Unknown event type: " + event.getClass().getName());
        }
    }
    
    /**
     * Extrae el notificationId de un evento para logging.
     */
    private String extractNotificationId(NotificationEvent event) {
        return switch (event) {
            case NotificationSentEvent sent -> sent.notificationId();
            case NotificationFailedEvent failed -> failed.notificationId();
            default -> "unknown";
        };
    }

    private void updateStatusStore(NotificationResult result) {
        NotificationStatus domainNotificationStatus = new NotificationStatus(
                result.success(),
                result.notificationId().toString(),
                result.providerName(),
                result.errorMessage());

        // El notificationId es el mismo que el transactionId porque los adaptadores
        // usan Notification.builder().id(transaction.id()) al crear la notificación
        String transactionId = result.notificationId().toString();
        
        // Mantener "FAILED" cuando hay error y asegurar que el errorMessage se guarde
        String status = result.success() ? "COMPLETED" : "FAILED";
        
        TransactionStatusInfo statusInfo = new TransactionStatusInfo(
                transactionId,
                status,
                domainNotificationStatus);

        statusPort.save(statusInfo);
        
        // Log adicional para verificar que se está actualizando
        System.out.println("[AUDIT] Updated transaction " + transactionId + 
                " to status " + status + 
                (result.errorMessage() != null ? " with error: " + result.errorMessage() : ""));
    }
}
