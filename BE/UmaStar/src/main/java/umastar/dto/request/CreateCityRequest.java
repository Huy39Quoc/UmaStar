package umastar.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import umastar.util.InputNormalizer;

import java.util.UUID;

/**
 * Yêu cầu tạo thành phố ({@code POST /api/v1/admin/cities}).
 * {@code timezone} là múi giờ IANA (VD: Asia/Ho_Chi_Minh), được kiểm tra ở tầng service.
 */
public record CreateCityRequest(

        @NotNull(message = "Vui lòng chọn quốc gia")
        UUID countryId,

        @NotBlank(message = "Vui lòng nhập tên thành phố")
        @Size(max = 150, message = "Tên thành phố tối đa 150 ký tự")
        String name,

        @Size(max = 150, message = "Tên tiếng Việt tối đa 150 ký tự")
        String nameVi,

        @Size(max = 150, message = "Tỉnh/bang/vùng tối đa 150 ký tự")
        String region,

        @NotBlank(message = "Vui lòng nhập múi giờ")
        @Size(max = 60, message = "Múi giờ tối đa 60 ký tự")
        String timezone,

        boolean hasAirport) {

    public CreateCityRequest {
        name = InputNormalizer.trim(name);
        nameVi = InputNormalizer.blankToNull(nameVi);
        region = InputNormalizer.blankToNull(region);
        timezone = InputNormalizer.trim(timezone);
    }
}
