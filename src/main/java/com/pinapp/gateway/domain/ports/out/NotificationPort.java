package com.pinapp.gateway.domain.ports.out;

import com.pinapp.gateway.domain.model.Transaction;

public interface NotificationPort {
    void sendSuccessNotification(Transaction t);

    void sendPendingNotification(Transaction t);

    void sendFailureNotification(Transaction t);
}
