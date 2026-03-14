package com.ajay.service;

import java.util.List;

import com.ajay.model.Order;
import com.ajay.model.Seller;
import com.ajay.model.Transaction;
import com.ajay.model.User;

public interface TransactionService {

    Transaction createTransaction(Order order);
    List<Transaction> getTransactionBySeller(Seller seller);
    List<Transaction>getAllTransactions();
}
