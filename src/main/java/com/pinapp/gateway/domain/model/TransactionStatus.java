package com.pinapp.gateway.domain.model;

/**
 * Enum que representa los posibles estados de una transacción financiera.
 * <p>
 * Este enum es parte del dominio y define las reglas de negocio para la selección
 * del canal de notificación. Cada estado dispara una estrategia de notificación específica:
 * </p>
 * <ul>
 *   <li><strong>COMPLETED</strong>: Se activa cuando una transacción se procesa exitosamente.
 *       Dispara notificación por <strong>Email</strong> (canal de alta prioridad para confirmaciones).</li>
 *   <li><strong>PENDING</strong>: Se activa cuando una transacción está en proceso de validación.
 *       Dispara notificación por <strong>Push</strong> (canal no bloqueante para actualizaciones de estado).</li>
 *   <li><strong>REJECTED</strong>: Se activa cuando una transacción es rechazada por validaciones de negocio.
 *       Dispara notificación por <strong>SMS</strong> (canal de alerta inmediata para situaciones críticas).</li>
 * </ul>
 * <p>
 * La lógica de mapeo estado -> canal está implementada en {@link com.pinapp.gateway.application.usecase.ProcessTransactionUseCase}.
 * </p>
 *
 * @author PinApp Gateway Team
 * @since 1.0.0
 * @see com.pinapp.gateway.application.usecase.ProcessTransactionUseCase
 */
public enum TransactionStatus {
    /**
     * Transacción completada exitosamente.
     * Dispara notificación por Email.
     */
    COMPLETED,
    
    /**
     * Transacción en proceso de validación.
     * Dispara notificación por Push.
     */
    PENDING,
    
    /**
     * Transacción rechazada por validaciones de negocio.
     * Dispara notificación por SMS.
     */
    REJECTED
}
