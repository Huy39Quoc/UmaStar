package umastar.service;

import umastar.dto.request.LoginRequest;
import umastar.dto.request.RegisterRequest;
import umastar.dto.response.AuthResponse;
import umastar.dto.response.RegisterResponse;

/** Nghiệp vụ xác thực & tài khoản. Các chức năng đăng nhập, refresh, đăng xuất... sẽ bổ sung dần vào đây. */
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
}
