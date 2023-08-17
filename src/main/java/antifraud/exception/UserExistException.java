package antifraud.exception;

public class UserExistException extends RuntimeException {
    public UserExistException(){
        super("User exist!");
    }
}
