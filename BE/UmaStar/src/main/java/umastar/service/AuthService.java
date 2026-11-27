package umastar.service;

import umastar.dto.request.RegisterRequest;
import umastar.dto.response.RegisterResponse;

/** Nghiệp vụ xác thực & tài khoản. Các chức năng đăng nhập, refresh, đăng xuất... sẽ bổ sung dần vào đây. */
public interface AuthService {

    /**
     * Đăng ký tài khoản khách hàng: tạo {@code User} (role CUSTOMER) và hồ sơ {@code Customer} trong cùng một transaction.
     *
     * @throws umastar.exception.AppException EMAIL_ALREADY_EXISTS nếu email đã được dùng (không phân biệt hoa thường)
     */
    RegisterResponse register(RegisterRequest request);
}
