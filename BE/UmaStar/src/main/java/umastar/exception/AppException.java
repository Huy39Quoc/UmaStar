package umastar.exception;

/**
 * Loi nghiep vu co kiem soat. Dung:
 *   throw new AppException(ErrorCode.HORSE_NOT_FOUND);
 *   throw new AppException(ErrorCode.BUSINESS_RULE_VIOLATION, "Ngày khởi hành phải sau hôm nay ít nhất 14 ngày");
 * Ban thong bao rieng chi dung khi can chi tiet dong, con lai dung message mac dinh cua ErrorCode.
 */
public class AppException extends RuntimeException {

    private final ErrorCode errorCode;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public AppException(ErrorCode errorCode, String message) {
        super(message != null ? message : errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}