package umastar.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import umastar.util.InputNormalizer;

import java.util.Locale;

/**
 * Yêu cầu cập nhật toàn bộ thông tin quốc gia ({@code PUT /api/v1/admin/countries/{id}}).
 * {@code active} bắt buộc: gửi {@code true} để kích hoạt lại một quốc gia đã bị vô hiệu hóa (xóa mềm).
 */
public record UpdateCountryRequest(

        @NotBlank(message = "Vui lòng nhập mã quốc gia")
        @Pattern(regexp = "[A-Z]{2}", message = "Mã quốc gia gồm đúng 2 chữ cái in hoa (ISO 3166-1 alpha-2)")
        String isoCode,

        @NotBlank(message = "Vui lòng nhập tên quốc gia")
        @Size(max = 100, message = "Tên quốc gia tối đa 100 ký tự")
        String name,

        @Size(max = 100, message = "Tên tiếng Việt tối đa 100 ký tự")
        String nameVi,

        boolean homeCountry,

        boolean partner,

        @NotNull(message = "Vui lòng cho biết quốc gia có đang hoạt động hay không")
        Boolean active) {

    public UpdateCountryRequest {
        String code = InputNormalizer.trim(isoCode);
        isoCode = code == null ? null : code.toUpperCase(Locale.ROOT);
        name = InputNormalizer.trim(name);
        nameVi = InputNormalizer.blankToNull(nameVi);
    }
}
