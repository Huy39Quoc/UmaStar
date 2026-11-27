package umastar.dto.response;

import umastar.enums.Role;
import umastar.enums.UserStatus;

import java.time.Instant;
import java.util.UUID;

/** Thông tin người dùng trả ra cho API hồ sơ cá nhân và quản lý tài khoản (không chứa thông tin nhạy cảm). */
public record UserResponse(
        UUID id,
        String fullName,
        String email,
        String phone,
        Role role,
        UserStatus status,
        boolean emailVerified,
        Instant lastLoginAt,
        Instant createdAt) {
}

