package umastar.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import umastar.dto.response.ErrorResponse;
import umastar.dto.response.ErrorResponse.FieldViolation;

import java.util.List;
import java.util.Map;

/**
 * Bo hung loi tap trung cho toan bo Controller.
 * Moi loi phat sinh se duoc doi thanh JSON ErrorResponse thong nhat.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Bang anh xa cac loi co san cua Spring sang ErrorCode tuong ung
    private static final Map<Class<? extends Exception>, ErrorCode> SPRING_ERRORS = Map.of(
            HttpMessageNotReadableException.class, ErrorCode.MALFORMED_REQUEST,
            MethodArgumentTypeMismatchException.class, ErrorCode.MALFORMED_REQUEST,
            AuthenticationException.class, ErrorCode.UNAUTHORIZED,
            AccessDeniedException.class, ErrorCode.FORBIDDEN,
            NoResourceFoundException.class, ErrorCode.RESOURCE_NOT_FOUND,
            ObjectOptimisticLockingFailureException.class, ErrorCode.CONCURRENT_UPDATE,
            MaxUploadSizeExceededException.class, ErrorCode.FILE_TOO_LARGE
    );

    // 1. Loi nghiep vu do code minh chu dong nem ra (throw new ApiException(...))
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApi(ApiException ex, HttpServletRequest req) {
        return build(ex.getErrorCode(), ex.getMessage(), req, List.of());
    }

    // 2. Loi validate du lieu dau vao (@Valid tren Request DTO) -> tra chi tiet tung field
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidBody(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<FieldViolation> fields = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new FieldViolation(e.getField(), e.getDefaultMessage()))
                .toList();
        return build(ErrorCode.VALIDATION_FAILED, null, req, fields);
    }

    // 3. Loi validate tren @RequestParam / @PathVariable
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleValidParam(ConstraintViolationException ex, HttpServletRequest req) {
        List<FieldViolation> fields = ex.getConstraintViolations().stream()
                .map(v -> new FieldViolation(v.getPropertyPath().toString(), v.getMessage()))
                .toList();
        return build(ErrorCode.VALIDATION_FAILED, null, req, fields);
    }

    // 4. Tat ca loi chuan cua Spring (sai JSON, chua login, khong du quyen, 404, xung dot @Version, file qua lon)
    @ExceptionHandler({
            HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            AuthenticationException.class, AccessDeniedException.class,
            NoResourceFoundException.class, ObjectOptimisticLockingFailureException.class,
            MaxUploadSizeExceededException.class
    })
    public ResponseEntity<ErrorResponse> handleSpringError(Exception ex, HttpServletRequest req) {
        ErrorCode code = SPRING_ERRORS.entrySet().stream()
                .filter(entry -> entry.getKey().isInstance(ex))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(ErrorCode.INTERNAL_ERROR);
        return build(code, null, req, List.of());
    }

    // 5. Loi he thong chua luong truoc -> ghi log server va tra 500 an toan
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnknown(Exception ex, HttpServletRequest req) {
        log.error("Unhandled error at {}", req.getRequestURI(), ex);
        return build(ErrorCode.INTERNAL_ERROR, null, req, List.of());
    }

    private ResponseEntity<ErrorResponse> build(
            ErrorCode code, String customMsg, HttpServletRequest req, List<FieldViolation> errors) {
        return ResponseEntity.status(code.getStatus())
                .body(ErrorResponse.of(code, customMsg, req.getRequestURI(), errors));
    }
}
