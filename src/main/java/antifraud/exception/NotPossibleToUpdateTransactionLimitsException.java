package antifraud.exception;

public class NotPossibleToUpdateTransactionLimitsException extends RuntimeException {

    public NotPossibleToUpdateTransactionLimitsException(){
        super("Not Possible To Update Transaction Limits! Feedback and validity matching!");
    }
}
