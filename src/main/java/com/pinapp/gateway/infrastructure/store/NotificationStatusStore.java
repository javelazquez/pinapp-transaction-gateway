package com.pinapp.gateway.infrastructure.store;

import com.pinapp.gateway.infrastructure.rest.dto.TransactionStatusDTO;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationStatusStore {

    private final ConcurrentHashMap<String, TransactionStatusDTO> store = new ConcurrentHashMap<>();

    public void updateStatus(String id, TransactionStatusDTO status) {
        store.put(id, status);
    }

    public Optional<TransactionStatusDTO> getStatus(String id) {
        return Optional.ofNullable(store.get(id));
    }
}
