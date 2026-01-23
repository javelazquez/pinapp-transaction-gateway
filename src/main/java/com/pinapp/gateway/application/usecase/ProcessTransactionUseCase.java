package com.pinapp.gateway.application.usecase;

import com.pinapp.gateway.domain.model.NotificationStatus;
import com.pinapp.gateway.domain.model.ProcessingResult;
import com.pinapp.gateway.domain.model.Transaction;
import com.pinapp.gateway.domain.model.TransactionStatus;
import com.pinapp.gateway.domain.ports.in.TransactionService;
import com.pinapp.gateway.domain.ports.out.NotificationPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ProcessTransactionUseCase implements TransactionService {

    private final NotificationPort emailAdapter;
    private final NotificationPort smsAdapter;
    private final NotificationPort pushAdapter;

    public ProcessTransactionUseCase(
            @Qualifier("emailAdapter") NotificationPort emailAdapter,
            @Qualifier("smsAdapter") NotificationPort smsAdapter,
            @Qualifier("pushAdapter") NotificationPort pushAdapter
    ) {
        this.emailAdapter = emailAdapter;
        this.smsAdapter = smsAdapter;
        this.pushAdapter = pushAdapter;
    }

    @Override
    public ProcessingResult process(Transaction transaction) {
        TransactionStatus status = transaction.status();

        NotificationStatus notificationStatus = switch (status) {
            case COMPLETED -> emailAdapter.notify(transaction, "¡Pago Exitoso!");
            case PENDING -> pushAdapter.notify(transaction, "Tu pago está siendo procesado.");
            case REJECTED -> smsAdapter.notify(transaction, "Alerta: Transacción Rechazada.");
        };

        return new ProcessingResult(transaction, notificationStatus);
    }
}
