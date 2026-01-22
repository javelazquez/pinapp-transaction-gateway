package com.pinapp.gateway.domain.ports.in;

import com.pinapp.gateway.domain.model.Transaction;
import com.pinapp.gateway.domain.model.ProcessingResult;

public interface TransactionService {
    ProcessingResult process(Transaction transaction);
}
