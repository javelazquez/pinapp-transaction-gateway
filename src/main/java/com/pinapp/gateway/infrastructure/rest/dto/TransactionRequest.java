package com.pinapp.gateway.infrastructure.rest.dto;

import com.pinapp.gateway.domain.model.TransactionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Solicitud para crear o procesar una transacción")
public record TransactionRequest(
                @Schema(description = "Identificador único de la transacción", example = "550e8400-e29b-41d4-a716-446655440000") UUID id,

                @Schema(description = "Monto de la transacción", example = "1500.0") BigDecimal amount,

                @Schema(description = "Nombre completo del cliente", example = "Juan Perez") String customerName,

                @Schema(description = "Correo electrónico del cliente", example = "juan.perez@example.com") String email,

                @Schema(description = "Teléfono de contacto", example = "+541112345678") String phone,

                @Schema(description = "Estado inicial de la transacción", example = "COMPLETED") TransactionStatus status,

                @Schema(description = "Token del dispositivo para notificaciones push (opcional)", example = "f_test_device_token_123456789") String deviceToken) {
}
