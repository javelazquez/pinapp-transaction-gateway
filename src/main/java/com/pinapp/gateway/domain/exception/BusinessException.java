package com.pinapp.gateway.domain.exception;

/**
 * Excepción base para errores relacionados con reglas de negocio en la capa de dominio.
 * <p>
 * <strong>Cuándo lanzar esta excepción:</strong>
 * </p>
 * <ul>
 *   <li>Cuando se viola una regla de negocio definida en el dominio</li>
 *   <li>Cuando una operación no puede completarse debido a restricciones del dominio</li>
 *   <li>Cuando se detecta un estado inválido de una entidad de dominio</li>
 * </ul>
 * <p>
 * <strong>Ejemplos de uso:</strong>
 * </p>
 * <ul>
 *   <li>Transacción con monto inválido (negativo, cero, excede límites)</li>
 *   <li>Intento de procesar una transacción en un estado incompatible</li>
 *   <li>Validaciones de integridad de datos del dominio</li>
 * </ul>
 * <p>
 * Esta excepción pertenece al dominio y no debe tener dependencias de frameworks
 * externos, manteniendo la pureza de la capa de dominio en Arquitectura Hexagonal.
 * </p>
 *
 * @author PinApp Gateway Team
 * @since 1.0.0
 */
public class BusinessException extends RuntimeException {
    /**
     * Crea una nueva excepción de negocio con el mensaje especificado.
     *
     * @param message Mensaje descriptivo del error de negocio
     */
    public BusinessException(String message) {
        super(message);
    }

    /**
     * Crea una nueva excepción de negocio con el mensaje y la causa especificados.
     *
     * @param message Mensaje descriptivo del error de negocio
     * @param cause La causa raíz de la excepción (puede ser null)
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
