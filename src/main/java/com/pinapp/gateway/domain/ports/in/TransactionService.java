package com.pinapp.gateway.domain.ports.in;

import com.pinapp.gateway.domain.model.Transaction;

public interface TransactionService {
    void process(Transaction transaction);
}
