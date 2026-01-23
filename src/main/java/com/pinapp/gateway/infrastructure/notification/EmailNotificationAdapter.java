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
 * Adaptador de infraestructura para notificaciones por Email.
 * <p>
 * Implementa {@link NotificationPort} y se conecta con el SDK de notificaciones
 * utilizando el servicio específico de Email configurado.
 * </p>
 *
 * @author PinApp Gateway Team
 * @since 1.0.0
 */
@Component("emailAdapter")
public class EmailNotificationAdapter implements NotificationPort {

    private final NotificationService emailNotificationService;

    public EmailNotificationAdapter(@Qualifier("emailNotificationService") NotificationService emailNotificationService) {
        this.emailNotificationService = emailNotificationService;
    }

    @Override
    public NotificationStatus notify(Transaction transaction, String message) {
        System.out.println("[EMAIL-ADAPTER] Processing notification for transaction: " + transaction.id());
        
        Recipient recipient = new Recipient(
                transaction.email(), 
                transaction.phone(), 
                Map.of("customerId", transaction.customerName())
        );
        
        Notification notification = Notification.create(recipient, message);
        
        NotificationResult result = emailNotificationService.send(notification, ChannelType.EMAIL);
        
        System.out.println("[EMAIL-ADAPTER] Notification sent via EMAIL channel. Status: " + 
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
