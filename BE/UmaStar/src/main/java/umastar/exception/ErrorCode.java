package umastar.exception;

/**
 * Danh sach ma loi chuan cua API kem HTTP status va thong bao mac dinh (tieng Anh).
 */
public enum ErrorCode {

    // 400 - Bad Request
    VALIDATION_FAILED(400, "Validation failed"),
    MALFORMED_REQUEST(400, "Malformed request"),

    // 401 - Unauthorized
    UNAUTHORIZED(401, "Authentication required"),
    INVALID_CREDENTIALS(401, "Invalid email or password"),
    TOKEN_INVALID(401, "Invalid token"),
    TOKEN_EXPIRED(401, "Token has expired"),

    // 403 - Forbidden
    FORBIDDEN(403, "You do not have permission to perform this action"),
    ACCOUNT_LOCKED(403, "Account is temporarily locked"),
    ACCOUNT_DISABLED(403, "Account is disabled"),

    // 404 - Not Found
    RESOURCE_NOT_FOUND(404, "Resource not found"),

    // 409 - Conflict
    CONFLICT(409, "Data conflict"),
    DUPLICATE_RESOURCE(409, "Resource already exists"),
    INVALID_STATE_TRANSITION(409, "Invalid state transition"),
    CONCURRENT_UPDATE(409, "Resource was updated by another user, please reload"),

    // 413 / 415 - File upload
    FILE_TOO_LARGE(413, "File exceeds maximum allowed size"),
    UNSUPPORTED_FILE_TYPE(415, "Unsupported file type"),

    // 422 / 429 / 500
    BUSINESS_RULE_VIOLATION(422, "Business rule violation"),
    TOO_MANY_REQUESTS(429, "Too many requests, please try again later"),
    INTERNAL_ERROR(500, "Internal server error");

    private final int status;
    private final String defaultMessage;

    ErrorCode(int status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public int getStatus() {
        return status;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
