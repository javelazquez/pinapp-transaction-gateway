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
 * Adaptador de infraestructura (Outbound Adapter) para notificaciones Push.
 * <p>
 * <strong>Responsabilidad en Arquitectura Hexagonal:</strong>
 * </p>
 * <p>
 * Este adaptador implementa el puerto {@link NotificationPort} definido por el dominio,
 * actuando como puente entre el dominio y el SDK externo de notificaciones. Su responsabilidad
 * es traducir los modelos del dominio ({@link com.pinapp.gateway.domain.model.Transaction})
 * al modelo del SDK ({@link com.pinapp.notify.domain.Notification}) y ejecutar el envío
 * a través del canal Push.
 * </p>
 * <p>
 * <strong>Por qué existe este adaptador:</strong>
 * </p>
 * <ul>
 *   <li>Mantiene el dominio libre de dependencias del SDK de PinApp</li>
 *   <li>Encapsula la lógica de mapeo entre modelos de dominio y modelos del SDK</li>
 *   <li>Permite cambiar la implementación del SDK sin afectar el dominio</li>
 *   <li>Facilita el testing al poder mockear el puerto en lugar del SDK</li>
 * </ul>
 * <p>
 * <strong>Requisito especial:</strong> Para notificaciones Push, el SDK requiere un
 * {@code deviceToken} en los metadatos del destinatario. Este token debe venir del
 * modelo {@link com.pinapp.gateway.domain.model.Transaction#deviceToken()}.
 * </p>
 * <p>
 * El adaptador utiliza {@code @Qualifier("pushNotificationService")} para inyectar
 * el servicio específico de Push configurado en {@link com.pinapp.gateway.infrastructure.config.PushConfig}.
 * </p>
 *
 * @author PinApp Gateway Team
 * @since 1.0.0
 * @see NotificationPort
 * @see com.pinapp.gateway.infrastructure.config.PushConfig
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
