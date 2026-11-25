package umastar.exception;

/**
 * Loi nghiep vu chung cho toan bo Service.
 * Khi can bao loi ve Client, chi can throw:
 *   throw new ApiException(ErrorCode.FORBIDDEN);
 *   hoac throw ApiException.notFound("Order", id);
 */
public class ApiException extends RuntimeException {

    private final ErrorCode errorCode;

    public ApiException(ErrorCode errorCode) {
        this(errorCode, errorCode.getDefaultMessage());
    }

    public ApiException(ErrorCode errorCode, String message) {
        super(message != null ? message : errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    // ---- Helper tao loi nhanh hay dung trong Service ----

    public static ApiException notFound(String resource, Object id) {
        return new ApiException(ErrorCode.RESOURCE_NOT_FOUND, resource + " not found with id: " + id);
    }

    public static ApiException duplicate(String message) {
        return new ApiException(ErrorCode.DUPLICATE_RESOURCE, message);
    }

    public static ApiException invalidTransition(String message) {
        return new ApiException(ErrorCode.INVALID_STATE_TRANSITION, message);
    }

    public static ApiException businessRule(String message) {
        return new ApiException(ErrorCode.BUSINESS_RULE_VIOLATION, message);
    }
}
