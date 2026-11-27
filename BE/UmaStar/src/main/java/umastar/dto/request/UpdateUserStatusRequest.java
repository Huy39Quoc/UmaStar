package umastar.dto.request;

import jakarta.validation.constraints.NotNull;
import umastar.enums.UserStatus;

/** Yêu cầu cập nhật trạng thái tài khoản nhân viên ({@code PATCH /api/v1/users/{id}/status}). */
public record UpdateUserStatusRequest(

        @NotNull(message = "Vui lòng chọn trạng thái tài khoản")
        UserStatus status) {
}

