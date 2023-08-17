package antifraud.exception;

public class TransactionFeedbackAlreadyExistException extends RuntimeException {

    public TransactionFeedbackAlreadyExistException(){
        super("Transaction feedback already exist!");
    }
}
