package hr.algebra.iis.okta.common;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public record ApiErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors,
        List<String> validationErrors
) {

    public static ApiErrorResponse of(int status, String error, String message, String path) {
        return new ApiErrorResponse(OffsetDateTime.now(), status, error, message, path, Map.of(), List.of());
    }

    public static ApiErrorResponse withFieldErrors(
            int status,
            String error,
            String message,
            String path,
            Map<String, String> fieldErrors
    ) {
        return new ApiErrorResponse(OffsetDateTime.now(), status, error, message, path, fieldErrors, List.of());
    }

    public static ApiErrorResponse withValidationErrors(
            int status,
            String error,
            String message,
            String path,
            List<String> validationErrors
    ) {
        return new ApiErrorResponse(OffsetDateTime.now(), status, error, message, path, Map.of(), validationErrors);
    }
}
