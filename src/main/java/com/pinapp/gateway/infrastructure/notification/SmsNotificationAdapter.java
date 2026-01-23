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
 * Adaptador de infraestructura para notificaciones por SMS.
 * <p>
 * Implementa {@link NotificationPort} y se conecta con el SDK de notificaciones
 * utilizando el servicio específico de SMS configurado.
 * </p>
 *
 * @author PinApp Gateway Team
 * @since 1.0.0
 */
@Component("smsAdapter")
public class SmsNotificationAdapter implements NotificationPort {

    private final NotificationService smsNotificationService;

    public SmsNotificationAdapter(@Qualifier("smsNotificationService") NotificationService smsNotificationService) {
        this.smsNotificationService = smsNotificationService;
    }

    @Override
    public NotificationStatus notify(Transaction transaction, String message) {
        System.out.println("[SMS-ADAPTER] Processing notification for transaction: " + transaction.id());
        
        Recipient recipient = new Recipient(
                transaction.email(), 
                transaction.phone(), 
                Map.of("customerId", transaction.customerName())
        );
        
        Notification notification = Notification.create(recipient, message);
        
        NotificationResult result = smsNotificationService.send(notification, ChannelType.SMS);
        
        System.out.println("[SMS-ADAPTER] Notification sent via SMS channel. Status: " + 
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
