package umastar.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Locale;

/**
 * Yêu cầu đăng nhập. Chỉ kiểm tra "có nhập" và độ dài tối đa, KHÔNG kiểm tra chính sách mật khẩu hay định dạng email:
 * tài khoản cũ có thể có mật khẩu không theo chính sách mới, và không muốn tiết lộ quy tắc cho kẻ dò mật khẩu.
 * Email được cắt khoảng trắng và viết thường; mật khẩu giữ nguyên và không bao giờ ghi ra log.
 */
public record LoginRequest(

        @NotBlank(message = "Vui lòng nhập email")
        @Size(max = 150, message = "Email tối đa 150 ký tự")
        String email,

        @NotBlank(message = "Vui lòng nhập mật khẩu")
        @Size(max = 128, message = "Mật khẩu tối đa 128 ký tự")
        String password) {

    public LoginRequest {
        email = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    /** Che mật khẩu để lỡ ghi log đối tượng này cũng không lộ thông tin. */
    @Override
    public String toString() {
        return "LoginRequest[email=" + email + ", password=***]";
    }
}
