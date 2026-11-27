package umastar.dto.response;

import umastar.enums.CustomerType;
import umastar.enums.Role;

import java.util.UUID;

/** Kết quả đăng ký thành công. Không chứa mật khẩu hay hash. Người dùng đăng nhập ở bước tiếp theo. */
public record RegisterResponse(
        UUID userId,
        UUID customerId,
        String fullName,
        String email,
        Role role,
        String organizationName,
        CustomerType customerType) {
}
