package umastar.dto.response;

import umastar.exception.ErrorCode;
import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        List<FieldViolation> errors,
        String path) {

    public record FieldViolation(String field, String message) {
    }

    public static ErrorResponse of(ErrorCode code, String message, String path) {
        return of(code, message, path, List.of());
    }

    public static ErrorResponse of(ErrorCode code, String message, String path, List<FieldViolation> errors) {
        return new ErrorResponse(
                Instant.now(),
                code.getStatus(),
                code.name(),
                message != null ? message : code.getDefaultMessage(),
                errors != null ? errors : List.of(),
                path);
    }
}
