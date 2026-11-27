package umastar.dto.response;

import umastar.enums.Role;

import java.util.UUID;

/** Thông tin tóm tắt của người dùng trả về cho FE (không có mật khẩu hay trạng thái bảo mật). */
public record UserSummaryResponse(UUID id, String fullName, String email, Role role) {
}
