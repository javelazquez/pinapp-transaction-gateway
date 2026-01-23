package com.pinapp.gateway.domain.ports.out;

import com.pinapp.gateway.domain.model.Transaction;
import com.pinapp.gateway.domain.model.NotificationStatus;

import java.util.concurrent.CompletableFuture;

/**
 * Puerto de salida (Outbound Port) para notificaciones en la Arquitectura Hexagonal.
 * <p>
 * Este puerto define el contrato que el dominio requiere para enviar notificaciones
 * a través de diferentes canales. Representa una abstracción que permite al dominio
 * comunicarse con el mundo exterior sin conocer los detalles de implementación.
 * </p>
 * <p>
 * <strong>Responsabilidad en Hexagonal Architecture:</strong>
 * </p>
 * <ul>
 *   <li>Define la interfaz que el dominio necesita (no cómo se implementa)</li>
 *   <li>Permite que el dominio permanezca independiente de frameworks y SDKs externos</li>
 *   <li>Las implementaciones (adaptadores) residen en la capa de infraestructura</li>
 * </ul>
 * <p>
 * Cada implementación especializada ({@code EmailNotificationAdapter}, {@code SmsNotificationAdapter},
 * {@code PushNotificationAdapter}) se encarga de mapear el modelo del dominio al modelo del SDK
 * y ejecutar el envío a través de su canal específico.
 * </p>
 *
 * @author PinApp Gateway Team
 * @since 1.0.0
 * @see com.pinapp.gateway.infrastructure.notification.EmailNotificationAdapter
 * @see com.pinapp.gateway.infrastructure.notification.SmsNotificationAdapter
 * @see com.pinapp.gateway.infrastructure.notification.PushNotificationAdapter
 */
public interface NotificationPort {
    /**
     * Envía una notificación para una transacción a través del canal implementado por el adaptador.
     *
     * @param transaction La transacción de dominio que contiene los datos del destinatario
     * @param message El mensaje de la notificación definido por la lógica de negocio
     * @return El estado de la notificación enviada, incluyendo éxito/fallo y detalles del proveedor
     */
    NotificationStatus notify(Transaction transaction, String message);

    /**
     * Envía una notificación de forma asíncrona (fire-and-forget) para una transacción.
     * <p>
     * Este método está diseñado para operaciones asíncronas donde no se necesita esperar
     * el resultado inmediatamente. La actualización del estado de la notificación ocurre
     * mediante eventos del SDK, que son capturados por suscriptores registrados
     * (como {@link com.pinapp.gateway.infrastructure.notification.TransactionAuditListener}).
     * </p>
     * <p>
     * <strong>Comportamiento:</strong>
     * </p>
     * <ul>
     *   <li>Retorna un {@link CompletableFuture} que se completa cuando la notificación
     *       es enviada al SDK (no cuando se procesa completamente)</li>
     *   <li>El procesamiento real y la actualización de estado ocurren en segundo plano</li>
     *   <li>Los eventos de éxito/fallo son manejados por los suscriptores del SDK</li>
     * </ul>
     * <p>
     * <strong>Uso típico:</strong> Procesamiento en lote (batch) donde se necesita
     * disparar múltiples notificaciones sin bloquear el hilo principal.
     * </p>
     *
     * @param transaction La transacción de dominio que contiene los datos del destinatario
     * @param message El mensaje de la notificación definido por la lógica de negocio
     * @return Un {@link CompletableFuture} que se completa cuando la notificación es enviada al SDK
     */
    CompletableFuture<Void> sendAsync(Transaction transaction, String message);
}
