package com.pinapp.gateway.infrastructure.notification;

import com.pinapp.gateway.domain.model.Transaction;
import com.pinapp.gateway.domain.ports.out.NotificationPort;
import org.springframework.stereotype.Component;

@Component
public class MockNotificationAdapter implements NotificationPort {

    @Override
    public void sendSuccessNotification(Transaction t) {
        System.out.println("MOCK: Notificando estado COMPLETED para transaccion " + t.id());
    }

    @Override
    public void sendPendingNotification(Transaction t) {
        System.out.println("MOCK: Notificando estado PENDING para transaccion " + t.id());
    }

    @Override
    public void sendFailureNotification(Transaction t) {
        System.out.println("MOCK: Notificando estado REJECTED para transaccion " + t.id());
    }
}
