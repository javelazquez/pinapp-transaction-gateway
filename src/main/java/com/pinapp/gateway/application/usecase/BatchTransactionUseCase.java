package com.pinapp.gateway.application.usecase;

import com.pinapp.gateway.domain.model.Transaction;
import com.pinapp.gateway.domain.ports.in.BatchTransactionService;
import com.pinapp.gateway.domain.ports.out.NotificationPort;
import com.pinapp.gateway.infrastructure.rest.dto.TransactionStatusDTO;
import com.pinapp.gateway.infrastructure.store.NotificationStatusStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BatchTransactionUseCase implements BatchTransactionService {

    private final NotificationPort notificationPort;
    private final NotificationStatusStore statusStore;

    public BatchTransactionUseCase(NotificationPort notificationPort, NotificationStatusStore statusStore) {
        this.notificationPort = notificationPort;
        this.statusStore = statusStore;
    }

    @Override
    public List<String> processBatch(List<Transaction> transactions) {
        List<String> transactionIds = new ArrayList<>();

        for (Transaction transaction : transactions) {
            // Ensure ID is generated if not present, though Transaction model likely has
            // it.
            // Assumption: Transaction ID is used.
            String id = transaction.id().toString();
            transactionIds.add(id);

            // Register initial status
            statusStore.updateStatus(id, new TransactionStatusDTO(id, "PROCESSING", null));

            // Async call
            notificationPort.sendAsync(transaction);
        }

        return transactionIds;
    }
}
