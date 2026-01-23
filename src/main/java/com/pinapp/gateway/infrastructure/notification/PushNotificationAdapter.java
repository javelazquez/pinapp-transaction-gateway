package com.pinapp.gateway.infrastructure.notification;

import com.pinapp.gateway.domain.model.NotificationStatus;
import com.pinapp.gateway.domain.model.Transaction;
import com.pinapp.gateway.domain.ports.out.NotificationPort;
import com.pinapp.notify.domain.Notification;
import com.pinapp.notify.domain.NotificationResult;
import com.pinapp.notify.domain.Recipient;
import com.pinapp.notify.domain.vo.ChannelType;
import com.pinapp.notify.ports.in.NotificationService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Adaptador de infraestructura para notificaciones Push.
 * <p>
 * Implementa {@link NotificationPort} y se conecta con el SDK de notificaciones
 * utilizando el servicio específico de Push configurado.
 * </p>
 *
 * @author PinApp Gateway Team
 * @since 1.0.0
 */
@Component("pushAdapter")
public class PushNotificationAdapter implements NotificationPort {

    private final NotificationService pushNotificationService;

    public PushNotificationAdapter(@Qualifier("pushNotificationService") NotificationService pushNotificationService) {
        this.pushNotificationService = pushNotificationService;
    }

    @Override
    public NotificationStatus notify(Transaction transaction, String message) {
        System.out.println("[PUSH-ADAPTER] Processing notification for transaction: " + transaction.id());
        
        // El deviceToken debe venir de la transacción
        if (transaction.deviceToken() == null || transaction.deviceToken().isBlank()) {
            System.out.println("[PUSH-ADAPTER] WARNING: deviceToken no proporcionado en la transacción. " +
                    "La notificación push puede fallar.");
        }
        
        Recipient recipient = new Recipient(
                transaction.email(), 
                transaction.phone(), 
                Map.of(
                    "customerId", transaction.customerName(),
                    "deviceToken", transaction.deviceToken() != null ? transaction.deviceToken() : ""
                )
        );
        
        Notification notification = Notification.create(recipient, message);
        
        NotificationResult result = pushNotificationService.send(notification, ChannelType.PUSH);
        
        System.out.println("[PUSH-ADAPTER] Notification sent via PUSH channel. Status: " + 
                (result.success() ? "SUCCESS" : "FAILED"));
        
        return mapToStatus(result);
    }

    private NotificationStatus mapToStatus(NotificationResult result) {
        return new NotificationStatus(
                result.success(),
                result.notificationId().toString(),
                result.providerName(),
                result.errorMessage()
        );
    }
}
