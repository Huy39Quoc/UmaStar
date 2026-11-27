package umastar.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import umastar.util.InputNormalizer;
import umastar.validation.StrongPassword;

/**
 * Yêu cầu đặt lại mật khẩu bằng token nhận từ email quên mật khẩu.
 * Token được cắt khoảng trắng hai đầu; mật khẩu mới giữ nguyên và bị che khi ghi log.
 */
public record ResetPasswordRequest(

        @NotBlank(message = "Vui lòng cung cấp mã đặt lại mật khẩu")
        @Size(max = 512, message = "Mã đặt lại mật khẩu không hợp lệ")
        String token,

        @NotBlank(message = "Vui lòng nhập mật khẩu mới")
        @StrongPassword
        String newPassword) {

    public ResetPasswordRequest {
        token = InputNormalizer.trim(token);
    }

    /** Che token và mật khẩu để không lộ thông tin nếu ghi log. */
    @Override
    public String toString() {
        return "ResetPasswordRequest[token=***, newPassword=***]";
    }
}
