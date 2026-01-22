package com.pinapp.gateway.infrastructure.store;

import com.pinapp.gateway.domain.model.TransactionStatusInfo;
import com.pinapp.gateway.domain.ports.out.TransactionStatusPort;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationStatusStore implements TransactionStatusPort {

    private final ConcurrentHashMap<String, TransactionStatusInfo> store = new ConcurrentHashMap<>();

    @Override
    public void save(TransactionStatusInfo statusInfo) {
        store.put(statusInfo.id(), statusInfo);
    }

    @Override
    public Optional<TransactionStatusInfo> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }
}
