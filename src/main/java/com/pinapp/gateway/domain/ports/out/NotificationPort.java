package com.pinapp.gateway.domain.ports.out;

import com.pinapp.gateway.domain.model.Transaction;
import com.pinapp.gateway.domain.model.NotificationStatus;

/**
 * Puerto de salida genérico para notificaciones.
 * <p>
 * Define el contrato para enviar notificaciones de transacciones
 * a través de diferentes canales (Email, SMS, Push).
 * </p>
 * <p>
 * Cada implementación especializada se encarga de enviar la notificación
 * a través de su canal específico.
 * </p>
 *
 * @author PinApp Gateway Team
 * @since 1.0.0
 */
public interface NotificationPort {
    /**
     * Envía una notificación para una transacción.
     *
     * @param transaction La transacción a notificar
     * @param message El mensaje de la notificación
     * @return El estado de la notificación enviada
     */
    NotificationStatus notify(Transaction transaction, String message);
}
