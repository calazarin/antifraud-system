package antifraud.exception;

public class DuplicatedStolenCardException extends RuntimeException {

    public DuplicatedStolenCardException() {
        super("Duplicated stolen card!");
    }
}
