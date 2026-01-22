package com.pinapp.gateway.infrastructure.rest;

import com.pinapp.gateway.domain.model.Transaction;
import com.pinapp.gateway.domain.ports.in.TransactionService;
import com.pinapp.gateway.infrastructure.rest.dto.NotificationSummaryResponse;
import com.pinapp.gateway.infrastructure.rest.dto.TransactionRequest;
import com.pinapp.gateway.infrastructure.rest.dto.TransactionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/transactions")
@Tag(name = "Transactions", description = "Endpoints para la gestión de transacciones")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Operation(summary = "Procesar una nueva transacción", description = "Recibe los detalles de una transacción, la procesa y gatilla las notificaciones correspondientes.")
    @ApiResponse(responseCode = "200", description = "Transacción procesada correctamente")
    @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    @PostMapping
    public org.springframework.http.ResponseEntity<TransactionResponse> processTransaction(
            @RequestBody(description = "Detalles de la transacción a procesar") @org.springframework.web.bind.annotation.RequestBody TransactionRequest request) {
        Transaction transaction = new Transaction(
                request.id(),
                request.amount(),
                request.customerName(),
                request.email(),
                request.phone(),
                request.status());

        com.pinapp.gateway.domain.model.ProcessingResult result = transactionService.process(transaction);

        NotificationSummaryResponse notificationResponse = new NotificationSummaryResponse(
                result.notificationStatus().success(),
                result.notificationStatus().messageId(),
                result.notificationStatus().provider(),
                result.notificationStatus().errorMessage());

        TransactionResponse response = new TransactionResponse(
                result.transaction().id(),
                result.transaction().amount(),
                result.transaction().status().name(),
                notificationResponse);

        return org.springframework.http.ResponseEntity.ok(response);
    }
}
