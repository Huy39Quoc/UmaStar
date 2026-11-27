package umastar.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import umastar.util.InputNormalizer;

/**
 * Yêu cầu gửi hướng dẫn đặt lại mật khẩu qua email. Email được cắt khoảng trắng và viết thường.
 */
public record ForgotPasswordRequest(

        @NotBlank(message = "Vui lòng nhập email")
        @Email(message = "Email không đúng định dạng")
        @Size(max = 150, message = "Email tối đa 150 ký tự")
        String email) {

    public ForgotPasswordRequest {
        email = InputNormalizer.normalizeEmail(email);
    }
}
