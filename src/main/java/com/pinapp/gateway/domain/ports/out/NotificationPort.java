package com.pinapp.gateway.domain.ports.out;

import com.pinapp.gateway.domain.model.Transaction;
import com.pinapp.gateway.domain.model.NotificationStatus;

public interface NotificationPort {
    NotificationStatus sendSuccessNotification(Transaction t);

    NotificationStatus sendPendingNotification(Transaction t);

    NotificationStatus sendFailureNotification(Transaction t);

    java.util.concurrent.CompletableFuture<NotificationStatus> sendAsync(Transaction t);
}
