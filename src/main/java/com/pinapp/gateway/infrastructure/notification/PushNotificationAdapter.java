package com.pinapp.gateway.infrastructure.notification;

import com.pinapp.gateway.domain.model.NotificationStatus;
import com.pinapp.gateway.domain.model.Transaction;
import com.pinapp.gateway.domain.model.TransactionStatusInfo;
import com.pinapp.gateway.domain.ports.out.NotificationPort;
import com.pinapp.gateway.domain.ports.out.TransactionStatusPort;
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
    private final TransactionStatusPort statusPort;

    public PushNotificationAdapter(
            @Qualifier("pushNotificationService") NotificationService pushNotificationService,
            TransactionStatusPort statusPort) {
        this.pushNotificationService = pushNotificationService;
        this.statusPort = statusPort;
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
        
        // Usar el transactionId como notificationId para mantener la relación
        Notification notification = Notification.builder()
                .id(transaction.id())
                .recipient(recipient)
                .message(message)
                .build();
        
        NotificationResult result = pushNotificationService.send(notification, ChannelType.PUSH);
        
        System.out.println("[PUSH-ADAPTER] Notification sent via PUSH channel. Status: " + 
                (result.success() ? "SUCCESS" : "FAILED"));
        
        return mapToStatus(result);
    }

    /**
     * Envía una notificación de forma asíncrona (fire-and-forget) para una transacción.
     * <p>
     * Este método implementa el patrón fire-and-forget: dispara la notificación y retorna
     * inmediatamente sin esperar el resultado. El procesamiento real ocurre en segundo plano
     * y los eventos de éxito/fallo son capturados automáticamente por el
     * {@link TransactionAuditListener}, que está registrado como suscriptor del SDK.
     * </p>
     * <p>
     * <strong>Flujo asíncrono:</strong>
     * </p>
     * <ol>
     *   <li>Mapea la {@link Transaction} del dominio al modelo {@link Notification} del SDK</li>
     *   <li>Llama a {@code notificationService.sendAsync()} que retorna un
     *       {@link CompletableFuture} del SDK</li>
     *   <li>Transforma el resultado a {@link CompletableFuture}{@code <Void>} para indicar
     *       que no se espera el resultado</li>
     *   <li>El SDK procesa la notificación en segundo plano y emite eventos</li>
     *   <li>El {@link TransactionAuditListener} captura los eventos y actualiza el store
     *       automáticamente</li>
     * </ol>
     * <p>
     * <strong>Importante:</strong> No se debe llamar a métodos bloqueantes (como {@code .join()}
     * o {@code .get()}) sobre el {@link CompletableFuture} retornado. El método está diseñado
     * para ser usado en operaciones fire-and-forget.
     * </p>
     *
     * @param transaction La transacción de dominio que contiene los datos del destinatario
     * @param message El mensaje de la notificación definido por la lógica de negocio
     * @return Un {@link CompletableFuture} que se completa cuando la notificación es enviada al SDK
     */
    @Override
    public CompletableFuture<Void> sendAsync(Transaction transaction, String message) {
        System.out.println("[PUSH-ADAPTER] Dispatching async notification for transaction: " + transaction.id());
        
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
        
        // Usar el transactionId como notificationId para mantener la relación
        // Esto permite que TransactionAuditListener actualice el estado correcto
        Notification notification = Notification.builder()
                .id(transaction.id())
                .recipient(recipient)
                .message(message)
                .build();
        
        // Llamar al SDK de forma asíncrona y transformar a CompletableFuture<Void>
        CompletableFuture<NotificationResult> sdkFuture = pushNotificationService.sendAsync(notification, ChannelType.PUSH);
        
        String transactionId = transaction.id().toString();
        
        // Capturar el resultado y actualizar el estado si hay error
        // Esto es necesario porque el SDK puede no emitir eventos cuando falla la validación
        return sdkFuture.handle((result, throwable) -> {
            if (throwable != null) {
                // Si hay una excepción, actualizar el estado a FAILED
                String errorMessage = throwable.getMessage() != null ? throwable.getMessage() : "Error desconocido";
                System.out.println("[PUSH-ADAPTER] Exception in async notification for transaction " + transactionId + 
                        ": " + errorMessage);
                updateStatusOnError(transactionId, errorMessage);
                return null;
            } else if (result != null && !result.success()) {
                // Si el resultado indica fallo, actualizar el estado
                String errorMessage = result.errorMessage() != null ? result.errorMessage() : "Notificación fallida";
                System.out.println("[PUSH-ADAPTER] Async notification failed for transaction " + transactionId + 
                        ". Error: " + errorMessage);
                updateStatusOnError(transactionId, errorMessage);
            } else {
                // Si es exitoso, el TransactionAuditListener lo manejará vía eventos
                System.out.println("[PUSH-ADAPTER] Async notification dispatched for transaction " + transactionId + 
                        ". Final status will be updated by TransactionAuditListener.");
            }
            return null;
        });
    }

    /**
     * Actualiza el estado de la transacción a FAILED cuando hay un error.
     * <p>
     * Este método se usa como fallback cuando el SDK no emite eventos de error
     * (por ejemplo, en errores de validación tempranos).
     * </p>
     *
     * @param transactionId El ID de la transacción
     * @param errorMessage El mensaje de error
     */
    private void updateStatusOnError(String transactionId, String errorMessage) {
        NotificationStatus notificationStatus = new NotificationStatus(
                false,
                transactionId,
                "push",
                errorMessage);

        TransactionStatusInfo statusInfo = new TransactionStatusInfo(
                transactionId,
                "FAILED",
                notificationStatus);

        statusPort.save(statusInfo);
        System.out.println("[PUSH-ADAPTER] Updated transaction " + transactionId + 
                " to status FAILED with error: " + errorMessage);
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
