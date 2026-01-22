package com.pinapp.gateway.domain.core.service;

import com.pinapp.gateway.domain.model.Transaction;
import com.pinapp.gateway.domain.ports.in.TransactionService;
import com.pinapp.gateway.domain.ports.out.NotificationPort;

public class ProcessTransactionUseCase implements TransactionService {

    private final NotificationPort notificationPort;

    public ProcessTransactionUseCase(NotificationPort notificationPort) {
        this.notificationPort = notificationPort;
    }

    @Override
    public void process(Transaction transaction) {
        var status = transaction.status();

        switch (status) {
            case COMPLETED -> notificationPort.sendSuccessNotification(transaction);
            case PENDING -> notificationPort.sendPendingNotification(transaction);
            case REJECTED -> notificationPort.sendFailureNotification(transaction);
        }
    }
}
