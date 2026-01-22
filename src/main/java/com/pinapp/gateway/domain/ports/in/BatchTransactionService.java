package com.pinapp.gateway.domain.ports.in;

import com.pinapp.gateway.domain.model.Transaction;
import java.util.List;

public interface BatchTransactionService {
    List<String> processBatch(List<Transaction> transactions);
}
