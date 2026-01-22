package com.pinapp.gateway.infrastructure.notification;

import com.pinapp.gateway.domain.model.Transaction;
import com.pinapp.gateway.domain.ports.out.NotificationPort;
import com.pinapp.notify.domain.Notification;
import com.pinapp.notify.domain.Recipient;
import com.pinapp.notify.domain.vo.ChannelType;
import com.pinapp.notify.ports.in.NotificationService;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NotificationAdapter implements NotificationPort {

    private final NotificationService notificationService;

    public NotificationAdapter(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void sendSuccessNotification(Transaction t) {
        Recipient recipient = new Recipient(t.email(), t.phone(), Map.of("customerId", t.customerName()));
        Notification notification = Notification.create(recipient, "Transaction " + t.id() + " COMPLETED");
        notificationService.send(notification, ChannelType.EMAIL);
    }

    @Override
    public void sendPendingNotification(Transaction t) {
        Recipient recipient = new Recipient(t.email(), t.phone(), Map.of("customerId", t.customerName()));
        Notification notification = Notification.create(recipient, "Transaction " + t.id() + " PENDING");
        notificationService.send(notification, ChannelType.PUSH);
    }

    @Override
    public void sendFailureNotification(Transaction t) {
        Recipient recipient = new Recipient(t.email(), t.phone(), Map.of("customerId", t.customerName()));
        Notification notification = Notification.create(recipient, "Transaction " + t.id() + " REJECTED");
        notificationService.send(notification, ChannelType.SMS);
    }
}
