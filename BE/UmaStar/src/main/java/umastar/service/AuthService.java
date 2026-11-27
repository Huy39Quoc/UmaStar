package umastar.service;

import umastar.dto.request.ChangePasswordRequest;
import umastar.dto.request.ForgotPasswordRequest;
import umastar.dto.request.LoginRequest;
import umastar.dto.request.RefreshTokenRequest;
import umastar.dto.request.RegisterRequest;
import umastar.dto.request.ResetPasswordRequest;
import umastar.dto.response.AuthResponse;
import umastar.dto.response.MessageResponse;
import umastar.dto.response.RegisterResponse;

import java.util.UUID;

/** Nghiệp vụ xác thực & tài khoản. */
public interface AuthService {

    /**
     * Đăng ký tài khoản khách hàng: tạo {@code User} (role CUSTOMER) và hồ sơ {@code Customer} trong cùng một transaction.
     *
     * @throws umastar.exception.AppException EMAIL_ALREADY_EXISTS nếu email đã được dùng (không phân biệt hoa thường)
     */
    RegisterResponse register(RegisterRequest request);

    /**
     * Đăng nhập bằng email và mật khẩu: kiểm tra trạng thái/khóa tạm thời (sai 5 lần khóa 15 phút),
     * cấp cặp access token + refresh token (lưu SHA-256 hash) và ghi nhận {@code lastLoginAt}.
     */
    AuthResponse login(LoginRequest request, String userAgent, String ipAddress);

    /**
     * Làm mới phiên (refresh token rotation): thu hồi refresh token cũ, cấp cặp token mới trong cùng họ token.
     * Nếu token đã bị thu hồi mà bị dùng lại thì coi là bị đánh cắp và thu hồi toàn bộ họ token đó.
     *
     * @throws umastar.exception.AppException INVALID_REFRESH_TOKEN (không có/hết hạn), REFRESH_TOKEN_REVOKED (dùng lại token cũ),
     *                                        ACCOUNT_DISABLED hoặc ACCOUNT_LOCKED (tài khoản bị vô hiệu hóa/khóa)
     */
    AuthResponse refresh(RefreshTokenRequest request, String userAgent, String ipAddress);

    /**
     * Đăng xuất khỏi MỘT thiết bị: thu hồi toàn bộ họ token chứa refresh token được gửi lên (lý do {@code LOGOUT}).
     * Xác thực bằng chính refresh token nên vẫn dùng được khi access token đã hết hạn. Idempotent: token không tồn tại
     * hoặc đã bị thu hồi thì không báo lỗi, để không tiết lộ token nào có thật.
     */
    void logout(RefreshTokenRequest request);

    /** Đăng xuất khỏi TẤT CẢ thiết bị: thu hồi mọi refresh token còn hiệu lực của người dùng (lý do {@code LOGOUT_ALL}). */
    void logoutAll(UUID userId);

    /**
     * Đổi mật khẩu của người đang đăng nhập: kiểm tra mật khẩu hiện tại, mật khẩu mới phải khác mật khẩu cũ,
     * ghi {@code passwordChangedAt} (làm access token cũ hết hiệu lực) và thu hồi mọi refresh token (lý do {@code PASSWORD_CHANGED}).
     * Nhập sai mật khẩu hiện tại được tính vào cùng bộ đếm khóa tạm như đăng nhập sai.
     *
     * @throws umastar.exception.AppException INVALID_OLD_PASSWORD, NEW_PASSWORD_SAME_AS_OLD, ACCOUNT_LOCKED, USER_NOT_FOUND
     */
    void changePassword(UUID userId, ChangePasswordRequest request);

    /**
     * Yêu cầu đặt lại mật khẩu: luôn trả thông báo chung (200 OK) dù email có tồn tại hay không để tránh dò email,
     * giới hạn tần suất (rate limit) và gửi mã đặt lại một lần qua {@link MailService}.
     *
     * @throws umastar.exception.AppException TOO_MANY_REQUESTS nếu gửi liên tiếp trong thời gian chờ
     */
    MessageResponse forgotPassword(ForgotPasswordRequest request);

    /**
     * Đặt lại mật khẩu bằng mã từ email: kiểm tra token còn hạn, xóa thông tin token về {@code null}, cập nhật
     * mật khẩu mới, ghi {@code passwordChangedAt} và thu hồi toàn bộ refresh token hiện có.
     *
     * @throws umastar.exception.AppException INVALID_RESET_TOKEN, NEW_PASSWORD_SAME_AS_OLD, ACCOUNT_DISABLED, ACCOUNT_LOCKED
     */
    void resetPassword(ResetPasswordRequest request);
}
