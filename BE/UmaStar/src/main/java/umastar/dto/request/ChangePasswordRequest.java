package umastar.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import umastar.validation.StrongPassword;

/**
 * Yêu cầu đổi mật khẩu của người đang đăng nhập. Mật khẩu hiện tại chỉ kiểm tra "có nhập" (tài khoản cũ có thể có mật khẩu
 * không theo chính sách mới); mật khẩu mới phải đạt chính sách chung. Cả hai giữ nguyên, không cắt khoảng trắng và không
 * bao giờ được ghi ra log.
 */
public record ChangePasswordRequest(

        @NotBlank(message = "Vui lòng nhập mật khẩu hiện tại")
        @Size(max = 128, message = "Mật khẩu tối đa 128 ký tự")
        String currentPassword,

        @NotBlank(message = "Vui lòng nhập mật khẩu mới")
        @StrongPassword
        String newPassword) {

    /** Che mật khẩu để lỡ ghi log đối tượng này cũng không lộ thông tin. */
    @Override
    public String toString() {
        return "ChangePasswordRequest[currentPassword=***, newPassword=***]";
    }
}
