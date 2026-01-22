package com.pinapp.gateway.infrastructure.rest.controller;

import com.pinapp.gateway.domain.model.ProcessingResult;
import com.pinapp.gateway.domain.model.Transaction;
import com.pinapp.gateway.domain.model.TransactionStatusInfo;
import com.pinapp.gateway.domain.ports.in.BatchTransactionService;
import com.pinapp.gateway.domain.ports.in.TransactionService;
import com.pinapp.gateway.domain.ports.out.TransactionStatusPort;
import com.pinapp.gateway.infrastructure.rest.dto.NotificationSummaryResponse;
import com.pinapp.gateway.infrastructure.rest.dto.TransactionRequest;
import com.pinapp.gateway.infrastructure.rest.dto.TransactionResponse;
import com.pinapp.gateway.infrastructure.rest.dto.TransactionStatusDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/v1/transactions")
@Tag(name = "Transactions", description = "Endpoints para la gestión de transacciones")
public class TransactionController {

        private final TransactionService transactionService;
        private final BatchTransactionService batchTransactionService;
        private final TransactionStatusPort statusPort;

        public TransactionController(TransactionService transactionService,
                        BatchTransactionService batchTransactionService,
                        TransactionStatusPort statusPort) {
                this.transactionService = transactionService;
                this.batchTransactionService = batchTransactionService;
                this.statusPort = statusPort;
        }

        @Operation(summary = "Procesar una nueva transacción", description = "Recibe los detalles de una transacción, la procesa y gatilla las notificaciones correspondientes.")
        @ApiResponse(responseCode = "200", description = "Transacción procesada correctamente")
        @ApiResponse(responseCode = "400", description = "Solicitud inválida")
        @PostMapping
        public ResponseEntity<TransactionResponse> processTransaction(@RequestBody TransactionRequest request) {
                Transaction transaction = new Transaction(
                                request.id(),
                                request.amount(),
                                request.customerName(),
                                request.email(),
                                request.phone(),
                                request.status());

                ProcessingResult result = transactionService.process(transaction);

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

                return ResponseEntity.ok(response);
        }

        @PostMapping("/batch")
        public ResponseEntity<List<String>> processBatch(@RequestBody List<TransactionRequest> requests) {

                List<Transaction> transactions = new ArrayList<>();
                for (TransactionRequest request : requests) {
                        transactions.add(new Transaction(
                                        request.id(),
                                        request.amount(),
                                        request.customerName(),
                                        request.email(),
                                        request.phone(),
                                        request.status()));
                }

                List<String> ids = batchTransactionService.processBatch(transactions);
                return ResponseEntity.accepted().body(ids);
        }

        @GetMapping("/status/{id}")
        public ResponseEntity<TransactionStatusDTO> getStatus(
                        @PathVariable String id) {
                return statusPort.findById(id)
                                .map(this::mapToDTO)
                                .map(ResponseEntity::ok)
                                .orElse(ResponseEntity.notFound().build());
        }

        private TransactionStatusDTO mapToDTO(TransactionStatusInfo info) {
                NotificationSummaryResponse notificationDTO = null;
                if (info.notificationStatus() != null) {
                        notificationDTO = new NotificationSummaryResponse(
                                        info.notificationStatus().success(),
                                        info.notificationStatus().messageId(),
                                        info.notificationStatus().provider(),
                                        info.notificationStatus().errorMessage());
                }
                return new TransactionStatusDTO(info.id(), info.status(), notificationDTO);
        }
}
