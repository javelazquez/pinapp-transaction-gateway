package com.pinapp.gateway.domain.ports.out;

import com.pinapp.gateway.domain.model.Transaction;
import com.pinapp.gateway.domain.model.NotificationStatus;

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
}
