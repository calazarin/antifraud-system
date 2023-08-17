package antifraud.exception;

public class SuspiciousIpNotFoundException extends RuntimeException {

    public SuspiciousIpNotFoundException(){
        super("Suspicious ip does not exist!");
    }
}
