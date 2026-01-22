package com.pinapp.gateway.application.usecase;

import com.pinapp.gateway.domain.model.Transaction;
import com.pinapp.gateway.domain.model.TransactionStatus;
import com.pinapp.gateway.domain.ports.in.TransactionService;
import com.pinapp.gateway.domain.ports.out.NotificationPort;
import org.springframework.stereotype.Service;

@Service
public class ProcessTransactionUseCase implements TransactionService {

    private final NotificationPort notificationPort;

    public ProcessTransactionUseCase(NotificationPort notificationPort) {
        this.notificationPort = notificationPort;
    }

    @Override
    public void process(Transaction transaction) {
        TransactionStatus status = transaction.status();

        switch (status) {
            case COMPLETED -> notificationPort.sendSuccessNotification(transaction);
            case PENDING -> notificationPort.sendPendingNotification(transaction);
            case REJECTED -> notificationPort.sendFailureNotification(transaction);
        }
    }
}
