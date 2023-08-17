package antifraud.exception;

public class InvalidCardNumberException extends RuntimeException{
    public InvalidCardNumberException() {
        super("invalid card number!");
    }
}
