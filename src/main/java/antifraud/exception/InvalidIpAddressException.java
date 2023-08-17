package antifraud.exception;

public class InvalidIpAddressException extends RuntimeException {

    public InvalidIpAddressException(){
        super("Invalid IP address!");
    }
}
