package com.pinapp.gateway.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entidad de dominio que representa una transacción financiera.
 * <p>
 * Esta clase es parte del núcleo del dominio (Domain Layer) y no tiene dependencias
 * de frameworks externos. Representa la entidad central del negocio que orquesta
 * el procesamiento de pagos y la decisión de estrategias de notificación.
 * </p>
 * <p>
 * En el contexto de Arquitectura Hexagonal, esta entidad es pura y puede ser
 * utilizada independientemente de la infraestructura (Spring, bases de datos, etc.).
 * </p>
 *
 * @param id Identificador único de la transacción
 * @param amount Monto de la transacción
 * @param customerName Nombre completo del cliente
 * @param email Correo electrónico del cliente (usado para notificaciones por Email)
 * @param phone Número de teléfono del cliente (usado para notificaciones por SMS)
 * @param status Estado actual de la transacción que determina la estrategia de notificación
 * @param deviceToken Token del dispositivo móvil (requerido para notificaciones Push)
 *
 * @author PinApp Gateway Team
 * @since 1.0.0
 */
public record Transaction(
    UUID id,
    BigDecimal amount,
    String customerName,
    String email,
    String phone,
    TransactionStatus status,
    String deviceToken
) {}
