package antifraud.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.context.support.DefaultMessageSourceResolvable;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        String errMsg = ex.getBindingResult().getAllErrors()
                .stream().map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(","));

        return buildResponseEntity(new ApiError(HttpStatus.BAD_REQUEST,
                Optional.of(errMsg), request.getDescription(false)));
    }

    @ExceptionHandler(value = {ConstraintViolationException.class, InvalidUserActionException.class,
            InvalidIpAddressException.class})
    protected ResponseEntity<Object> bandBadRequests(RuntimeException ex, WebRequest request) {
        return buildResponseEntity(new ApiError(HttpStatus.BAD_REQUEST, Optional.of(ex.getMessage()),
                request.getDescription(false)));
    }

    @ExceptionHandler(value = {UserNotFoundException.class,
            RoleNotFoundException.class,
            SuspiciousIpNotFoundException.class,
            StolenCardNotFoundException.class,
            TransactionNotFoundException.class})
    protected ResponseEntity<Object> handleNotFoundException(RuntimeException ex, WebRequest request) {
        return buildResponseEntity(new ApiError(HttpStatus.NOT_FOUND, Optional.of(ex.getMessage()),
                request.getDescription(false)));
    }

    private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
        return new ResponseEntity<>(apiError, HttpStatusCode.valueOf(apiError.getStatus()));
    }

    @ExceptionHandler({AuthenticationException.class})
    @ResponseBody
    public ResponseEntity<Object> handleAuthenticationException(RuntimeException ex, WebRequest request) {
        log.error("AuthenticationException!!", ex);
        return buildResponseEntity(new ApiError(HttpStatus.UNAUTHORIZED, Optional.of(ex.getMessage()),
                request.getDescription(false)));
    }

    @ExceptionHandler({AccessDeniedException.class})
    @ResponseBody
    public ResponseEntity<Object> handleAccessDeniedException(RuntimeException ex, WebRequest request) {
        log.error("AccessDeniedException!!", ex);
        return buildResponseEntity(new ApiError(HttpStatus.FORBIDDEN, Optional.of("Access Denied!"),
                request.getDescription(false)));
    }

    @ExceptionHandler({RoleDoesNotExistException.class,
            InvalidCardNumberException.class,
            InvalidTransactionFeedback.class})
    public ResponseEntity<Object> handleBadRequests(RuntimeException ex, WebRequest request) {
        return buildResponseEntity(new ApiError(HttpStatus.BAD_REQUEST, Optional.of(ex.getMessage()),
                request.getDescription(false)));
    }

    @ExceptionHandler({DuplicatedRoleException.class,
            DuplicatedSuspiciousIpException.class,
            UserExistException.class,
            DuplicatedStolenCardException.class,
            TransactionFeedbackAlreadyExistException.class})
    public ResponseEntity<Object> handleDuplicatedException(RuntimeException ex, WebRequest request) {
        log.error("DuplicatedException!!", ex);
        return buildResponseEntity(new ApiError(HttpStatus.CONFLICT, Optional.of(ex.getMessage()),
                request.getDescription(false)));
    }

    @ExceptionHandler({NotPossibleToUpdateTransactionLimitsException.class})
    public ResponseEntity<Object> handleNotPossibleToUpdateTransactionLimitsException(RuntimeException ex, WebRequest request) {
        log.error("NotPossibleToUpdateTransactionLimitsException!!", ex);
        return buildResponseEntity(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, Optional.of(ex.getMessage()),
                request.getDescription(false)));
    }
}
