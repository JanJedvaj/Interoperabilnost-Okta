package hr.algebra.iis.okta.common;

import hr.algebra.iis.okta.application.validation.ApplicationImportValidationException;
import hr.algebra.iis.okta.okta.exception.OktaApiException;
import hr.algebra.iis.okta.okta.exception.OktaClientException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return ResponseEntity
                .badRequest()
                .body(ApiErrorResponse.withFieldErrors(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        "Request validation failed",
                        request.getRequestURI(),
                        fieldErrors
                ));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(
                        HttpStatus.NOT_FOUND.value(),
                        HttpStatus.NOT_FOUND.getReasonPhrase(),
                        exception.getMessage(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of(
                        HttpStatus.CONFLICT.value(),
                        HttpStatus.CONFLICT.getReasonPhrase(),
                        "The application could not be saved because it violates a database constraint",
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(ApplicationImportValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleImportValidation(
            ApplicationImportValidationException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .badRequest()
                .body(ApiErrorResponse.withValidationErrors(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        exception.getMessage(),
                        request.getRequestURI(),
                        exception.getValidationErrors()
                ));
    }

    @ExceptionHandler(OktaApiException.class)
    public ResponseEntity<ApiErrorResponse> handleOktaApi(
            OktaApiException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_GATEWAY;

        if (exception.getStatusCode() == 401 || exception.getStatusCode() == 403) {
            status = HttpStatus.FORBIDDEN;
        } else if (exception.getStatusCode() == 404) {
            status = HttpStatus.NOT_FOUND;
        } else if (exception.getStatusCode() == 429) {
            status = HttpStatus.TOO_MANY_REQUESTS;
        }

        return ResponseEntity
                .status(status)
                .body(ApiErrorResponse.of(
                        status.value(),
                        status.getReasonPhrase(),
                        "Okta API returned status %d".formatted(exception.getStatusCode()),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(OktaClientException.class)
    public ResponseEntity<ApiErrorResponse> handleOktaClient(
            OktaClientException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(ApiErrorResponse.of(
                        HttpStatus.BAD_GATEWAY.value(),
                        HttpStatus.BAD_GATEWAY.getReasonPhrase(),
                        exception.getMessage(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ApiErrorResponse> handleAuthentication(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiErrorResponse.of(
                        HttpStatus.UNAUTHORIZED.value(),
                        HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                        "Authentication failed",
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .badRequest()
                .body(ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        exception.getMessage(),
                        request.getRequestURI()
                ));
    }
}
