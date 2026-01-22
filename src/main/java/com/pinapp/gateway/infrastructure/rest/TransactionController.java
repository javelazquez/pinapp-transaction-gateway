package com.pinapp.gateway.infrastructure.rest;

import com.pinapp.gateway.domain.model.Transaction;
import com.pinapp.gateway.domain.ports.in.TransactionService;
import com.pinapp.gateway.infrastructure.rest.dto.TransactionRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public void processTransaction(@RequestBody TransactionRequest request) {
        Transaction transaction = new Transaction(
                request.id(),
                request.amount(),
                request.customerName(),
                request.email(),
                request.phone(),
                request.status());

        transactionService.process(transaction);
    }
}
