package antifraud.exception;

public class DuplicatedRoleException extends RuntimeException{

    public DuplicatedRoleException(){
        super("Duplicated role!");
    }
}
