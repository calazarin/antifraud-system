package antifraud.exception;

public class RoleDoesNotExistException extends RuntimeException{

    public RoleDoesNotExistException(){
        super("Role does not exist!");
    }
}
