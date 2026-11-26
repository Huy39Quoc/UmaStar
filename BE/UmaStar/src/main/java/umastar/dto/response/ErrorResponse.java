package umastar.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import umastar.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        Instant timestamp,
        int status,
        int code,
        String error,
        String message,
        String path,
        @JsonInclude(JsonInclude.Include.NON_EMPTY) Map<String, String> errors) {

    public static ErrorResponse of(ErrorCode errorCode, String message, String path, Map<String, String> errors) {
        return new ErrorResponse(
                Instant.now(),
                errorCode.getHttpStatus().value(),
                errorCode.getCode(),
                errorCode.getHttpStatus().getReasonPhrase(),
                message != null ? message : errorCode.getMessage(),
                path,
                errors);
    }
}