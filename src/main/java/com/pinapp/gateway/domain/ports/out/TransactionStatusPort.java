package com.pinapp.gateway.domain.ports.out;

import com.pinapp.gateway.domain.model.TransactionStatusInfo;
import java.util.Optional;

/**
 * Port for managing and retrieving transaction statuses.
 */
public interface TransactionStatusPort {
    void save(TransactionStatusInfo statusInfo);

    Optional<TransactionStatusInfo> findById(String id);
}
