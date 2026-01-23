package com.pinapp.gateway.application.usecase;

import com.pinapp.gateway.domain.model.NotificationStatus;
import com.pinapp.gateway.domain.model.ProcessingResult;
import com.pinapp.gateway.domain.model.Transaction;
import com.pinapp.gateway.domain.model.TransactionStatus;
import com.pinapp.gateway.domain.ports.in.TransactionService;
import com.pinapp.gateway.domain.ports.out.NotificationPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Caso de uso principal que orquesta el procesamiento de transacciones y la selección
 * de estrategias de notificación.
 * <p>
 * <strong>Responsabilidad en Arquitectura Hexagonal:</strong>
 * </p>
 * <p>
 * Esta clase pertenece a la capa de Aplicación y actúa como orquestador que coordina
 * la lógica de negocio sin conocer los detalles de implementación de la infraestructura.
 * Implementa el patrón Use Case, encapsulando una operación de negocio específica.
 * </p>
 * <p>
 * <strong>Reglas de Negocio para Selección de Canal:</strong>
 * </p>
 * <ul>
 *   <li><strong>COMPLETED → Email</strong>: Las transacciones exitosas se notifican por Email
 *       (canal de alta prioridad para confirmaciones formales).</li>
 *   <li><strong>PENDING → Push</strong>: Las transacciones en proceso se notifican por Push
 *       (canal no bloqueante para actualizaciones de estado en tiempo real).</li>
 *   <li><strong>REJECTED → SMS</strong>: Las transacciones rechazadas se notifican por SMS
 *       (canal de alerta inmediata para situaciones críticas que requieren atención del usuario).</li>
 * </ul>
 * <p>
 * La inyección de múltiples implementaciones del mismo puerto ({@link NotificationPort}) mediante
 * {@code @Qualifier} permite que el caso de uso seleccione el adaptador correcto según la regla
 * de negocio, manteniendo el principio de inversión de dependencias.
 * </p>
 *
 * @author PinApp Gateway Team
 * @since 1.0.0
 * @see TransactionService
 * @see NotificationPort
 */
@Service
public class ProcessTransactionUseCase implements TransactionService {

    private final NotificationPort emailAdapter;
    private final NotificationPort smsAdapter;
    private final NotificationPort pushAdapter;

    public ProcessTransactionUseCase(
            @Qualifier("emailAdapter") NotificationPort emailAdapter,
            @Qualifier("smsAdapter") NotificationPort smsAdapter,
            @Qualifier("pushAdapter") NotificationPort pushAdapter
    ) {
        this.emailAdapter = emailAdapter;
        this.smsAdapter = smsAdapter;
        this.pushAdapter = pushAdapter;
    }

    /**
     * Procesa una transacción y selecciona el canal de notificación según su estado.
     * <p>
     * Este método implementa la lógica de negocio que determina qué adaptador de notificación
     * debe utilizarse basándose en el estado de la transacción. La decisión está encapsulada
     * en el switch, siguiendo las reglas de negocio definidas.
     * </p>
     *
     * @param transaction La transacción a procesar
     * @return Un resultado que combina la transacción original con el estado de la notificación enviada
     */
    @Override
    public ProcessingResult process(Transaction transaction) {
        TransactionStatus status = transaction.status();

        NotificationStatus notificationStatus = switch (status) {
            case COMPLETED -> emailAdapter.notify(transaction, "¡Pago Exitoso!");
            case PENDING -> pushAdapter.notify(transaction, "Tu pago está siendo procesado.");
            case REJECTED -> smsAdapter.notify(transaction, "Alerta: Transacción Rechazada.");
        };

        return new ProcessingResult(transaction, notificationStatus);
    }
}
