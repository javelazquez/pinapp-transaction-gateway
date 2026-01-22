package com.pinapp.gateway.application.usecase;

import com.pinapp.gateway.domain.model.Transaction;
import com.pinapp.gateway.domain.model.TransactionStatusInfo;
import com.pinapp.gateway.domain.ports.in.BatchTransactionService;
import com.pinapp.gateway.domain.ports.out.NotificationPort;
import com.pinapp.gateway.domain.ports.out.TransactionStatusPort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BatchTransactionUseCase implements BatchTransactionService {

    private final NotificationPort notificationPort;
    private final TransactionStatusPort statusPort;

    public BatchTransactionUseCase(NotificationPort notificationPort, TransactionStatusPort statusPort) {
        this.notificationPort = notificationPort;
        this.statusPort = statusPort;
    }

    @Override
    public List<String> processBatch(List<Transaction> transactions) {
        List<String> transactionIds = new ArrayList<>();

        for (Transaction transaction : transactions) {
            String id = transaction.id().toString();
            transactionIds.add(id);

            // Register initial status using domain model and port
            statusPort.save(new TransactionStatusInfo(id, "PROCESSING", null));

            // Async call
            notificationPort.sendAsync(transaction);
        }

        return transactionIds;
    }
}
