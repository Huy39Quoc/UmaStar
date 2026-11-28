package umastar.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import umastar.util.InputNormalizer;

import java.util.Locale;

/**
 * Yêu cầu tạo quốc gia ({@code POST /api/v1/admin/countries}).
 * Mã quốc gia được cắt khoảng trắng và tự viết hoa trước khi kiểm tra ({@code vn} thành {@code VN}).
 * {@code homeCountry} và {@code partner} mặc định {@code false} nếu không gửi.
 */
public record CreateCountryRequest(

        @NotBlank(message = "Vui lòng nhập mã quốc gia")
        @Pattern(regexp = "[A-Z]{2}", message = "Mã quốc gia gồm đúng 2 chữ cái in hoa (ISO 3166-1 alpha-2)")
        String isoCode,

        @NotBlank(message = "Vui lòng nhập tên quốc gia")
        @Size(max = 100, message = "Tên quốc gia tối đa 100 ký tự")
        String name,

        @Size(max = 100, message = "Tên tiếng Việt tối đa 100 ký tự")
        String nameVi,

        boolean homeCountry,

        boolean partner) {

    public CreateCountryRequest {
        String code = InputNormalizer.trim(isoCode);
        isoCode = code == null ? null : code.toUpperCase(Locale.ROOT);
        name = InputNormalizer.trim(name);
        nameVi = InputNormalizer.blankToNull(nameVi);
    }
}
