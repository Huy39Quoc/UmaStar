package umastar.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import umastar.enums.CustomerType;
import umastar.validation.StrongPassword;

import java.util.Locale;

/**
 * Yêu cầu đăng ký tài khoản khách hàng (CLB hoặc cá nhân chủ ngựa).
 * Dữ liệu được chuẩn hóa ngay khi tạo đối tượng: cắt khoảng trắng hai đầu, email viết thường,
 * số điện thoại rỗng thành {@code null}. Riêng mật khẩu giữ nguyên, không bao giờ bị cắt hay ghi ra log.
 */
public record RegisterRequest(

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

        @Pattern(regexp = "\\+?[0-9]{8,15}", message = "Số điện thoại gồm 8-15 chữ số, có thể bắt đầu bằng dấu +")
        String phone,

        @NotBlank(message = "Vui lòng nhập tên tổ chức hoặc chủ ngựa")
        @Size(max = 200, message = "Tên tổ chức tối đa 200 ký tự")
        String organizationName,

        @NotNull(message = "Vui lòng chọn loại khách hàng (CLUB hoặc INDIVIDUAL)")
        CustomerType customerType) {

    public RegisterRequest {
        fullName = trim(fullName);
        email = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
        phone = blankToNull(phone);
        organizationName = trim(organizationName);
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    private static String blankToNull(String value) {
        String trimmed = trim(value);
        return trimmed == null || trimmed.isEmpty() ? null : trimmed;
    }

    /** Che mật khẩu để lỡ ghi log đối tượng này cũng không lộ thông tin. */
    @Override
    public String toString() {
        return "RegisterRequest[fullName=" + fullName + ", email=" + email + ", password=***, phone=" + phone
                + ", organizationName=" + organizationName + ", customerType=" + customerType + "]";
    }
}
