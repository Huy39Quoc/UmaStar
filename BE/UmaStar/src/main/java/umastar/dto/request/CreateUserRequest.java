package umastar.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import umastar.enums.Role;
import umastar.util.InputNormalizer;
import umastar.validation.StrongPassword;
import umastar.validation.ValidPhone;

/**
 * Yêu cầu tạo tài khoản nhân viên do Logistics Manager thực hiện ({@code POST /api/v1/users}).
 */
public record CreateUserRequest(

        @NotBlank(message = "Vui lòng nhập họ tên")
        @Size(max = 150, message = "Họ tên tối đa 150 ký tự")
        String fullName,

        @NotBlank(message = "Vui lòng nhập email")
        @Email(message = "Email không đúng định dạng")
        @Size(max = 150, message = "Email tối đa 150 ký tự")
        String email,

        @NotBlank(message = "Vui lòng nhập mật khẩu")
        @StrongPassword
        String password,

        @ValidPhone
        String phone,

        @NotNull(message = "Vui lòng chọn vai trò nhân viên")
        Role role) {

    public CreateUserRequest {
        fullName = InputNormalizer.trim(fullName);
        email = InputNormalizer.normalizeEmail(email);
        phone = InputNormalizer.blankToNull(phone);
    }

    /** Che mật khẩu để lỡ ghi log đối tượng này cũng không lộ thông tin. */
    @Override
    public String toString() {
        return "CreateUserRequest[fullName=" + fullName + ", email=" + email + ", password=***, phone=" + phone
                + ", role=" + role + "]";
    }
}

