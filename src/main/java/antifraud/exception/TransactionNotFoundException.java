package antifraud.exception;

import antifraud.entity.Transaction;

public class TransactionNotFoundException extends RuntimeException {

    public TransactionNotFoundException(){
        super("Transaction not found!");
    }
}
