package com.pinapp.gateway.application.usecase;

import com.pinapp.gateway.domain.model.Transaction;
import com.pinapp.gateway.domain.model.TransactionStatusInfo;
import com.pinapp.gateway.domain.ports.in.BatchTransactionService;
import com.pinapp.gateway.domain.ports.out.NotificationPort;
import com.pinapp.gateway.domain.ports.out.TransactionStatusPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Caso de uso para el procesamiento en lote (batch) de transacciones.
 * <p>
 * <strong>Responsabilidad en Arquitectura Hexagonal:</strong>
 * </p>
 * <p>
 * Esta clase pertenece a la capa de Aplicación y gestiona el procesamiento asíncrono
 * de múltiples transacciones en un solo lote. A diferencia de {@link ProcessTransactionUseCase},
 * este caso de uso está optimizado para alto volumen y no bloquea la respuesta al cliente.
 * </p>
 * <p>
 * <strong>Estrategia de Notificación para Batch:</strong>
 * </p>
 * <p>
 * Utiliza el canal <strong>Push</strong> para todas las notificaciones de batch porque:
 * </p>
 * <ul>
 *   <li>Es un canal no bloqueante que permite procesar grandes volúmenes sin afectar
 *       el rendimiento del sistema</li>
 *   <li>Las notificaciones push son ideales para actualizaciones de estado en tiempo real
 *       que no requieren confirmación inmediata del usuario</li>
 *   <li>Permite procesar el lote de forma asíncrona mientras el cliente recibe una
 *       respuesta inmediata con los IDs de las transacciones registradas</li>
 * </ul>
 * <p>
 * <strong>Flujo de Ejecución:</strong>
 * </p>
 * <ol>
 *   <li>Registra cada transacción con estado "PROCESSING" en el puerto de persistencia</li>
 *   <li>Envía notificación push para cada transacción (procesamiento asíncrono)</li>
 *   <li>Retorna la lista de IDs de transacciones para que el cliente pueda consultar
 *       su estado posteriormente</li>
 * </ol>
 *
 * @author PinApp Gateway Team
 * @since 1.0.0
 * @see ProcessTransactionUseCase
 * @see BatchTransactionService
 */
@Service
public class BatchTransactionUseCase implements BatchTransactionService {

    private final NotificationPort pushAdapter;
    private final TransactionStatusPort statusPort;

    public BatchTransactionUseCase(
            @Qualifier("pushAdapter") NotificationPort pushAdapter,
            TransactionStatusPort statusPort
    ) {
        this.pushAdapter = pushAdapter;
        this.statusPort = statusPort;
    }

    /**
     * Procesa un lote de transacciones de forma asíncrona.
     * <p>
     * Este método registra cada transacción con estado inicial "PROCESSING" y envía
     * una notificación push para cada una. El procesamiento es asíncrono, permitiendo
     * que el método retorne inmediatamente con los IDs de las transacciones registradas.
     * </p>
     * <p>
     * El cliente puede consultar el estado de cada transacción posteriormente mediante
     * el endpoint de consulta de estado.
     * </p>
     *
     * @param transactions Lista de transacciones a procesar en lote
     * @return Lista de IDs de las transacciones registradas (en formato String)
     */
    @Override
    public List<String> processBatch(List<Transaction> transactions) {
        List<String> transactionIds = new ArrayList<>();

        for (Transaction transaction : transactions) {
            String id = transaction.id().toString();
            transactionIds.add(id);

            // Register initial status using domain model and port
            statusPort.save(new TransactionStatusInfo(id, "PROCESSING", null));

            // Send notification via Push channel for batch processing
            pushAdapter.notify(transaction, "Transaction " + transaction.id() + " PROCESSING");
        }

        return transactionIds;
    }
}
