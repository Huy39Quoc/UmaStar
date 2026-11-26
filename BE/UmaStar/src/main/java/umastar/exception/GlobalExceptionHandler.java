package umastar.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import umastar.dto.response.ErrorResponse;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Bo hung loi tap trung. Moi loi -> ErrorResponse thong nhat.
 * Quy tac: loi cua client (4xx) khong log stack trace; loi he thong (5xx) luon log day du.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String SQL_STATE_FOREIGN_KEY_VIOLATION = "23503";

    /**
     * Anh xa TEN rang buoc DB -> ma loi cu the (dung khi 2 request trung nhau cung luc,
     * buoc exists-check o service khong chan duoc). Ten rang buoc phai dat theo quy uoc uk_<bang>_<cot>
     * trong @Table(uniqueConstraints = ...) cua entity va viet thuong.
     */
    private static final Map<String, ErrorCode> CONSTRAINT_ERRORS = Map.of(
            "uk_users_email", ErrorCode.EMAIL_ALREADY_EXISTS,
            "uk_horses_microchip_no", ErrorCode.MICROCHIP_ALREADY_EXISTS,
            "uk_vehicles_code_or_plate", ErrorCode.VEHICLE_CODE_ALREADY_EXISTS,
            "uk_countries_iso_code", ErrorCode.COUNTRY_CODE_ALREADY_EXISTS,
            "uk_city_country_name", ErrorCode.CITY_NAME_ALREADY_EXISTS
    );

    // ========================= Loi nghiep vu =========================

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException ex, HttpServletRequest req) {
        ErrorCode errorCode = ex.getErrorCode();
        if (errorCode.getHttpStatus().is5xxServerError()) {
            log.error("AppException {} at {}", errorCode.name(), req.getRequestURI(), ex);
        } else {
            log.debug("AppException {} at {}: {}", errorCode.name(), req.getRequestURI(), ex.getMessage());
        }
        return build(errorCode, ex.getMessage(), req, null);
    }

    // ========================= Validation =========================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> errors = new LinkedHashMap<>();
        // Moi truong chi giu thong bao dau tien; loi cap class (vd mat khau xac nhan) nam duoi ten object.
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errors.putIfAbsent(e.getField(), messageOrDefault(e.getDefaultMessage())));
        ex.getBindingResult().getGlobalErrors()
                .forEach(e -> errors.putIfAbsent(e.getObjectName(), messageOrDefault(e.getDefaultMessage())));
        return build(ErrorCode.VALIDATION_FAILED, null, req, errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex,
                                                                   HttpServletRequest req) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(v -> {
            String path = v.getPropertyPath().toString();
            // Chi lay ten tham so cuoi cung, khong de lo ten method noi bo.
            String field = path.substring(path.lastIndexOf('.') + 1);
            errors.putIfAbsent(field, messageOrDefault(v.getMessage()));
        });
        return build(ErrorCode.VALIDATION_FAILED, null, req, errors);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleMethodValidation(HandlerMethodValidationException ex,
                                                                HttpServletRequest req) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getParameterValidationResults().forEach(result -> {
            String name = result.getMethodParameter().getParameterName();
            String field = name != null ? name : "parameter";
            result.getResolvableErrors()
                    .forEach(err -> errors.putIfAbsent(field, messageOrDefault(err.getDefaultMessage())));
        });
        return build(ErrorCode.VALIDATION_FAILED, null, req, errors);
    }

    // ========================= Loi HTTP cua Spring MVC =========================

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex, HttpServletRequest req) {
        return build(ErrorCode.INVALID_REQUEST, null, req, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex,
                                                                HttpServletRequest req) {
        return build(ErrorCode.METHOD_NOT_ALLOWED, null, req, null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex,
                                                                    HttpServletRequest req) {
        return build(ErrorCode.UNSUPPORTED_MEDIA_TYPE, null, req, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest req) {
        return build(ErrorCode.RESOURCE_NOT_FOUND, null, req, null);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex,
                                                                     HttpServletRequest req) {
        return build(ErrorCode.FILE_TOO_LARGE, null, req, null);
    }

    // ========================= Security (loi nem ra tu @PreAuthorize trong controller) =========================

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return build(ErrorCode.FORBIDDEN, null, req, null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex, HttpServletRequest req) {
        return build(ErrorCode.UNAUTHORIZED, null, req, null);
    }

    // ========================= Database =========================

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(OptimisticLockingFailureException ex,
                                                              HttpServletRequest req) {
        return build(ErrorCode.CONCURRENT_UPDATE, null, req, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex,
                                                             HttpServletRequest req) {
        log.warn("Data integrity violation at {}: {}", req.getRequestURI(), ex.getMostSpecificCause().getMessage());
        for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
            if (cause instanceof org.hibernate.exception.ConstraintViolationException violation) {
                String name = violation.getConstraintName();
                ErrorCode mapped = name == null ? null : CONSTRAINT_ERRORS.get(name.toLowerCase(Locale.ROOT));
                if (mapped != null) {
                    return build(mapped, null, req, null);
                }
                if (SQL_STATE_FOREIGN_KEY_VIOLATION.equals(violation.getSQLState())) {
                    return build(ErrorCode.RESOURCE_IN_USE, null, req, null);
                }
                break;
            }
        }
        return build(ErrorCode.DATA_INTEGRITY_VIOLATION, null, req, null);
    }

    // ========================= Loi khong luong truoc =========================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex, HttpServletRequest req) {
        log.error("Unhandled error at {}", req.getRequestURI(), ex);
        return build(ErrorCode.INTERNAL_SERVER_ERROR, null, req, null);
    }

    // ========================= Helper =========================

    private ResponseEntity<ErrorResponse> build(ErrorCode errorCode, String message, HttpServletRequest req,
                                                Map<String, String> errors) {
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ErrorResponse.of(errorCode, message, req.getRequestURI(), errors));
    }

    private static String messageOrDefault(String message) {
        return message != null ? message : "Giá trị không hợp lệ";
    }
}