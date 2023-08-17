package antifraud.exception;

public class InvalidTransactionFeedback extends RuntimeException {

    public InvalidTransactionFeedback(){
        super("Invalid transaction feedback!");
    }
}
