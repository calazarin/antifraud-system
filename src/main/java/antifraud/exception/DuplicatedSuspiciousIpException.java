package antifraud.exception;

public class DuplicatedSuspiciousIpException extends RuntimeException {

    public DuplicatedSuspiciousIpException(){
        super("Duplicated suspicious IP!");
    }
}
