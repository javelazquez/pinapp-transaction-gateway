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
 * <strong>Flujo de Ejecución Asíncrono:</strong>
 * </p>
 * <ol>
 *   <li>Registra cada transacción con estado "PROCESSING" en el puerto de persistencia</li>
 *   <li>Dispara notificación push de forma asíncrona (fire-and-forget) para cada transacción</li>
 *   <li>Retorna inmediatamente la lista de IDs de transacciones sin esperar el procesamiento</li>
 *   <li>El SDK procesa las notificaciones en segundo plano</li>
 *   <li>El {@link com.pinapp.gateway.infrastructure.notification.TransactionAuditListener}
 *       captura los eventos del SDK y actualiza automáticamente los estados finales
 *       ("COMPLETED" o "FAILED") en el store</li>
 * </ol>
 * <p>
 * <strong>Importante:</strong> Este caso de uso implementa el patrón fire-and-forget.
 * No bloquea el hilo principal esperando respuestas del SDK. La actualización de estado
 * final ocurre mediante eventos asíncronos manejados por el TransactionAuditListener.
 * </p>
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
     * Procesa un lote de transacciones de forma asíncrona (fire-and-forget).
     * <p>
     * Este método implementa un flujo verdaderamente asíncrono y no bloqueante:
     * </p>
     * <ul>
     *   <li>Registra cada transacción con estado inicial "PROCESSING" en el store</li>
     *   <li>Dispara notificaciones push de forma asíncrona sin esperar el resultado</li>
     *   <li>Retorna inmediatamente con los IDs de las transacciones registradas</li>
     * </ul>
     * <p>
     * <strong>Actualización de Estado mediante Eventos:</strong>
     * </p>
     * <p>
     * La actualización del estado final ("COMPLETED" o "FAILED") ocurre automáticamente
     * mediante eventos del SDK. El {@link com.pinapp.gateway.infrastructure.notification.TransactionAuditListener},
     * que está registrado como suscriptor del SDK, captura los eventos
     * ({@code NotificationSentEvent} o {@code NotificationFailedEvent}) y actualiza
     * el store con el estado final correspondiente.
     * </p>
     * <p>
     * <strong>No bloqueante:</strong> Este método no realiza llamadas bloqueantes
     * (como {@code .join()} o {@code .get()}) sobre los {@link java.util.concurrent.CompletableFuture}
     * retornados por el adaptador. El hilo principal retorna inmediatamente después
     * de disparar las notificaciones.
     * </p>
     * <p>
     * El cliente puede consultar el estado de cada transacción posteriormente mediante
     * el endpoint de consulta de estado. Inicialmente verá "PROCESSING" y luego
     * "COMPLETED" o "FAILED" una vez que el SDK procese la notificación y el listener
     * actualice el estado.
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

            // Dispatch async notification (fire-and-forget)
            // The TransactionAuditListener will update the final status via SDK events
            pushAdapter.sendAsync(transaction, "Transaction " + transaction.id() + " PROCESSING");
            // CRITICAL: No .join(), .get(), or any blocking call here
        }

        return transactionIds;
    }
}
