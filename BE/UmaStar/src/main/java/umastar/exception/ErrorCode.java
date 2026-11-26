package umastar.exception;

import org.springframework.http.HttpStatus;

/**
 * Ma loi thong nhat. Moi loi gom: ma so (code), thong bao hien thi cho nguoi dung, HTTP status.
 *
 * QUY UOC DAI MA (moi nhom 100 so, khong de trung):
 *   100 AUTH | 200 TOKEN | 300 USER | 400 PASSWORD | 500 FILE | 600 SYSTEM
 *   700 MASTER DATA (Country, City) | 800 VEHICLE | 900 REGULATION
 *   1000 CUSTOMER | 1100 HORSE | 1200 NOTIFICATION
 *   (du kien) 1300 ORDER | 1400 LEGAL_DOCUMENT | 1500 TRIP | 1600 TRIP_STOP, ASSIGNMENT
 *             1700 INCIDENT, ROUTE_CHANGE | 1800 HANDOVER | 1900 REPORT
 *
 * Them loi moi: chon so tiep theo trong dung nhom. ErrorCodeTest se bao loi neu bi trung so.
 */
public enum ErrorCode {

    // ========================= AUTH (100 - 199) =========================
    INVALID_CREDENTIALS(100, "Email hoặc mật khẩu không đúng", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(101, "Vui lòng đăng nhập để tiếp tục", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(102, "Bạn không có quyền thực hiện chức năng này", HttpStatus.FORBIDDEN),
    ACCOUNT_LOCKED(103, "Tài khoản tạm thời bị khóa do đăng nhập sai nhiều lần. Vui lòng thử lại sau", HttpStatus.LOCKED),
    ACCOUNT_DISABLED(104, "Tài khoản đã bị vô hiệu hóa", HttpStatus.FORBIDDEN),

    // ========================= TOKEN (200 - 299) =========================
    INVALID_TOKEN(200, "Phiên đăng nhập không hợp lệ", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(201, "Phiên đăng nhập đã hết hạn", HttpStatus.UNAUTHORIZED),
    INVALID_REFRESH_TOKEN(202, "Phiên đăng nhập không hợp lệ. Vui lòng đăng nhập lại", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_REVOKED(203, "Phiên đăng nhập đã bị thu hồi. Vui lòng đăng nhập lại", HttpStatus.UNAUTHORIZED),
    INVALID_RESET_TOKEN(204, "Mã đặt lại mật khẩu không hợp lệ hoặc đã hết hạn", HttpStatus.BAD_REQUEST),

    // ========================= USER (300 - 399) =========================
    USER_NOT_FOUND(300, "Không tìm thấy người dùng", HttpStatus.NOT_FOUND),
    EMAIL_ALREADY_EXISTS(301, "Email đã được sử dụng", HttpStatus.CONFLICT),
    INVALID_STAFF_ROLE(302, "Vai trò nhân viên không hợp lệ", HttpStatus.BAD_REQUEST),
    CANNOT_DEACTIVATE_SELF(303, "Bạn không thể tự vô hiệu hóa tài khoản của mình", HttpStatus.CONFLICT),
    LAST_MANAGER_PROTECTED(304, "Không thể vô hiệu hóa quản lý điều hành cuối cùng của hệ thống", HttpStatus.CONFLICT),

    // ========================= PASSWORD (400 - 499) =========================
    INVALID_OLD_PASSWORD(400, "Mật khẩu hiện tại không đúng", HttpStatus.BAD_REQUEST),
    NEW_PASSWORD_SAME_AS_OLD(401, "Mật khẩu mới phải khác mật khẩu cũ", HttpStatus.BAD_REQUEST),
    PASSWORD_MISMATCH(402, "Mật khẩu xác nhận không khớp", HttpStatus.BAD_REQUEST),

    // ========================= FILE (500 - 599) =========================
    // 413 va 422: dung valueOf(int) vi ten hang so cua Spring doi giua cac phien ban (PAYLOAD_TOO_LARGE, UNPROCESSABLE_ENTITY)
    FILE_TOO_LARGE(500, "File vượt quá dung lượng cho phép", HttpStatus.valueOf(413)),
    UNSUPPORTED_FILE_TYPE(501, "Định dạng file không được hỗ trợ", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    INVALID_FILE_NAME(502, "Tên file không hợp lệ", HttpStatus.BAD_REQUEST),
    FILE_NOT_FOUND(503, "Không tìm thấy file", HttpStatus.NOT_FOUND),
    FILE_STORAGE_ERROR(504, "Không thể lưu hoặc đọc file, vui lòng thử lại sau", HttpStatus.INTERNAL_SERVER_ERROR),

    // ========================= SYSTEM (600 - 699) =========================
    INTERNAL_SERVER_ERROR(600, "Lỗi hệ thống, vui lòng thử lại sau", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_REQUEST(601, "Yêu cầu không hợp lệ", HttpStatus.BAD_REQUEST),
    VALIDATION_FAILED(602, "Dữ liệu nhập chưa hợp lệ", HttpStatus.BAD_REQUEST),
    METHOD_NOT_ALLOWED(603, "Phương thức HTTP không được hỗ trợ", HttpStatus.METHOD_NOT_ALLOWED),
    UNSUPPORTED_MEDIA_TYPE(604, "Content-Type không được hỗ trợ", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    RESOURCE_NOT_FOUND(605, "Không tìm thấy dữ liệu yêu cầu", HttpStatus.NOT_FOUND),
    CONCURRENT_UPDATE(606, "Dữ liệu vừa được người khác cập nhật. Vui lòng tải lại và thử lại", HttpStatus.CONFLICT),
    DATA_INTEGRITY_VIOLATION(607, "Dữ liệu bị trùng hoặc vi phạm ràng buộc", HttpStatus.CONFLICT),
    RESOURCE_IN_USE(608, "Dữ liệu này đang được sử dụng nên không thể xóa. Hãy vô hiệu hóa thay thế", HttpStatus.CONFLICT),
    INVALID_STATE_TRANSITION(609, "Không thể chuyển sang trạng thái này", HttpStatus.CONFLICT),
    BUSINESS_RULE_VIOLATION(610, "Vi phạm quy tắc nghiệp vụ", HttpStatus.valueOf(422)),
    TOO_MANY_REQUESTS(611, "Thao tác quá nhanh, vui lòng thử lại sau", HttpStatus.TOO_MANY_REQUESTS),

    // ========================= MASTER DATA: COUNTRY, CITY (700 - 799) =========================
    COUNTRY_NOT_FOUND(700, "Không tìm thấy quốc gia", HttpStatus.NOT_FOUND),
    COUNTRY_CODE_ALREADY_EXISTS(701, "Mã quốc gia đã tồn tại", HttpStatus.CONFLICT),
    HOME_COUNTRY_ALREADY_EXISTS(702, "Hệ thống chỉ được có một quốc gia sở tại", HttpStatus.CONFLICT),
    CITY_NOT_FOUND(703, "Không tìm thấy thành phố", HttpStatus.NOT_FOUND),
    CITY_NAME_ALREADY_EXISTS(704, "Tên thành phố đã tồn tại trong quốc gia này", HttpStatus.CONFLICT),
    CITY_NOT_IN_COUNTRY(705, "Thành phố không thuộc quốc gia đã chọn", HttpStatus.BAD_REQUEST),
    INVALID_TIMEZONE(706, "Múi giờ không hợp lệ", HttpStatus.BAD_REQUEST),
    COUNTRY_NOT_PARTNER(707, "Quốc gia này chưa nằm trong danh sách hợp tác", HttpStatus.BAD_REQUEST),

    // ========================= VEHICLE (800 - 899) =========================
    VEHICLE_NOT_FOUND(800, "Không tìm thấy phương tiện", HttpStatus.NOT_FOUND),
    VEHICLE_CODE_ALREADY_EXISTS(801, "Biển số hoặc mã phương tiện đã tồn tại", HttpStatus.CONFLICT),
    VEHICLE_IN_USE(802, "Phương tiện đang gắn với chuyến vận chuyển nên không thể xóa hoặc chuyển sang bảo trì", HttpStatus.CONFLICT),
    VEHICLE_NOT_AVAILABLE(803, "Phương tiện hiện không sẵn sàng", HttpStatus.CONFLICT),

    // ========================= REGULATION (900 - 999) =========================
    REGULATION_NOT_FOUND(900, "Không tìm thấy quy định", HttpStatus.NOT_FOUND),
    REGULATION_DOCS_REQUIRED(901, "Quy định phải có ít nhất một loại giấy tờ bắt buộc", HttpStatus.BAD_REQUEST),

    // ========================= CUSTOMER (1000 - 1099) =========================
    CUSTOMER_NOT_FOUND(1000, "Không tìm thấy khách hàng", HttpStatus.NOT_FOUND),

    // ========================= HORSE (1100 - 1199) =========================
    HORSE_NOT_FOUND(1100, "Không tìm thấy ngựa hoặc ngựa không thuộc về bạn", HttpStatus.NOT_FOUND),
    MICROCHIP_ALREADY_EXISTS(1101, "Số microchip đã tồn tại trong hệ thống", HttpStatus.CONFLICT),
    HORSE_IN_ACTIVE_ORDER(1102, "Ngựa đang nằm trong đơn vận chuyển chưa hoàn tất", HttpStatus.CONFLICT),

    // ========================= NOTIFICATION (1200 - 1299) =========================
    NOTIFICATION_NOT_FOUND(1200, "Không tìm thấy thông báo", HttpStatus.NOT_FOUND);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}