package com.pinapp.gateway.infrastructure.notification;

import com.pinapp.gateway.domain.model.NotificationStatus;
import com.pinapp.gateway.domain.model.Transaction;
import com.pinapp.gateway.domain.ports.out.NotificationPort;
import com.pinapp.notify.domain.Notification;
import com.pinapp.notify.domain.NotificationResult;
import com.pinapp.notify.domain.Recipient;
import com.pinapp.notify.domain.vo.ChannelType;
import com.pinapp.notify.ports.in.NotificationService;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class NotificationAdapter implements NotificationPort {

    private final NotificationService notificationService;

    public NotificationAdapter(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public NotificationStatus sendSuccessNotification(Transaction t) {
        Recipient recipient = new Recipient(t.email(), t.phone(), Map.of("customerId", t.customerName()));
        Notification notification = Notification.create(recipient, "Transaction " + t.id() + " COMPLETED");
        NotificationResult result = notificationService.send(notification, ChannelType.EMAIL);
        return mapToStatus(result);
    }

    @Override
    public NotificationStatus sendPendingNotification(Transaction t) {
        Recipient recipient = new Recipient(t.email(), t.phone(), Map.of("customerId", t.customerName()));
        Notification notification = Notification.create(recipient, "Transaction " + t.id() + " PENDING");
        NotificationResult result = notificationService.send(notification, ChannelType.PUSH);
        return mapToStatus(result);
    }

    @Override
    public NotificationStatus sendFailureNotification(Transaction t) {
        Recipient recipient = new Recipient(t.email(), t.phone(), Map.of("customerId", t.customerName()));
        Notification notification = Notification.create(recipient, "Transaction " + t.id() + " REJECTED");
        NotificationResult result = notificationService.send(notification, ChannelType.SMS);
        return mapToStatus(result);
    }

    @Override
    public CompletableFuture<NotificationStatus> sendAsync(Transaction t) {
        Recipient recipient = new Recipient(t.email(), t.phone(), Map.of("customerId", t.customerName()));

        // Use Transaction ID as Notification ID to ensure correct status tracking in
        // Listener
        Notification notification = Notification.builder()
                .id(t.id())
                .recipient(recipient)
                .message("Transaction " + t.id() + " PROCESSING")
                .priority(com.pinapp.notify.domain.NotificationPriority.NORMAL)
                .build();

        return notificationService.sendAsync(notification)
                .thenApply(this::mapToStatus);
    }

    private NotificationStatus mapToStatus(NotificationResult result) {
        return new NotificationStatus(
                result.success(),
                result.notificationId().toString(),
                result.providerName(),
                result.errorMessage());
    }
}
