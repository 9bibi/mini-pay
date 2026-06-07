package com.minipay.exception;

import com.minipay.dto.ApiErrorResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleWalletNotFound(WalletNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(BadRequestException exception) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(IdempotencyConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleIdempotencyConflict(IdempotencyConflictException exception) {
        return error(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler({
            DuplicateEmailException.class,
            InsufficientFundsException.class,
            SameWalletTransferException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBusinessError(RuntimeException exception) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> fields = new HashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            fields.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.withFields(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        "Validation failed",
                        fields
                ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
        String message = exception.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));
        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodValidation(HandlerMethodValidationException exception) {
        return error(HttpStatus.BAD_REQUEST, "Validation failed");
    }

    private ResponseEntity<ApiErrorResponse> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(baseBody(status, message));
    }

    private ApiErrorResponse baseBody(HttpStatus status, String message) {
        return ApiErrorResponse.of(status.value(), status.getReasonPhrase(), message);
    }
}
