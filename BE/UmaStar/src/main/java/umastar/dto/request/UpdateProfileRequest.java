package umastar.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import umastar.util.InputNormalizer;
import umastar.validation.ValidPhone;

/**
 * Yêu cầu cập nhật hồ sơ cá nhân ({@code PUT /api/v1/profile}).
 * Không chứa {@code email} và {@code role} nên người dùng không thể tự thay đổi hai trường này.
 */
public record UpdateProfileRequest(

        @NotBlank(message = "Vui lòng nhập họ tên")
        @Size(max = 150, message = "Họ tên tối đa 150 ký tự")
        String fullName,

        @ValidPhone
        String phone) {

    public UpdateProfileRequest {
        fullName = InputNormalizer.trim(fullName);
        phone = InputNormalizer.blankToNull(phone);
    }
}

