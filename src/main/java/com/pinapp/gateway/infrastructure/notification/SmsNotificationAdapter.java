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
import java.util.concurrent.CompletableFuture;

/**
 * Adaptador de infraestructura (Outbound Adapter) para notificaciones por SMS.
 * <p>
 * <strong>Responsabilidad en Arquitectura Hexagonal:</strong>
 * </p>
 * <p>
 * Este adaptador implementa el puerto {@link NotificationPort} definido por el dominio,
 * actuando como puente entre el dominio y el SDK externo de notificaciones. Su responsabilidad
 * es traducir los modelos del dominio ({@link com.pinapp.gateway.domain.model.Transaction})
 * al modelo del SDK ({@link com.pinapp.notify.domain.Notification}) y ejecutar el envío
 * a través del canal SMS.
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
 * El adaptador utiliza {@code @Qualifier("smsNotificationService")} para inyectar
 * el servicio específico de SMS configurado en {@link com.pinapp.gateway.infrastructure.config.SmsConfig}.
 * </p>
 *
 * @author PinApp Gateway Team
 * @since 1.0.0
 * @see NotificationPort
 * @see com.pinapp.gateway.infrastructure.config.SmsConfig
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
        
        // Usar el transactionId como notificationId para mantener la relación
        Notification notification = Notification.builder()
                .id(transaction.id())
                .recipient(recipient)
                .message(message)
                .build();
        
        NotificationResult result = smsNotificationService.send(notification, ChannelType.SMS);
        
        System.out.println("[SMS-ADAPTER] Notification sent via SMS channel. Status: " + 
                (result.success() ? "SUCCESS" : "FAILED"));
        
        return mapToStatus(result);
    }

    /**
     * Envía una notificación de forma asíncrona (fire-and-forget) para una transacción.
     * <p>
     * Similar a {@link PushNotificationAdapter#sendAsync}, este método dispara la notificación
     * y retorna inmediatamente. El procesamiento real ocurre en segundo plano y los eventos
     * son capturados por el {@link TransactionAuditListener}.
     * </p>
     *
     * @param transaction La transacción de dominio que contiene los datos del destinatario
     * @param message El mensaje de la notificación definido por la lógica de negocio
     * @return Un {@link CompletableFuture} que se completa cuando la notificación es enviada al SDK
     */
    @Override
    public CompletableFuture<Void> sendAsync(Transaction transaction, String message) {
        System.out.println("[SMS-ADAPTER] Dispatching async notification for transaction: " + transaction.id());
        
        Recipient recipient = new Recipient(
                transaction.email(), 
                transaction.phone(), 
                Map.of("customerId", transaction.customerName())
        );
        
        // Usar el transactionId como notificationId para mantener la relación
        // Esto permite que TransactionAuditListener actualice el estado correcto
        Notification notification = Notification.builder()
                .id(transaction.id())
                .recipient(recipient)
                .message(message)
                .build();
        
        CompletableFuture<NotificationResult> sdkFuture = smsNotificationService.sendAsync(notification, ChannelType.SMS);
        
        return sdkFuture.thenAccept(result -> {
            System.out.println("[SMS-ADAPTER] Async notification dispatched. " +
                    "Final status will be updated by TransactionAuditListener. " +
                    "SDK Status: " + (result.success() ? "SUCCESS" : "FAILED"));
        });
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
