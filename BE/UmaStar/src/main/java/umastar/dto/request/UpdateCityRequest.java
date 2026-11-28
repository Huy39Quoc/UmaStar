package umastar.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import umastar.util.InputNormalizer;

/**
 * Yêu cầu cập nhật toàn bộ thông tin thành phố ({@code PUT /api/v1/admin/cities/{id}}). Không đổi được quốc gia của thành phố.
 * {@code active} bắt buộc: gửi {@code true} để kích hoạt lại thành phố đã bị vô hiệu hóa (xóa mềm).
 */
public record UpdateCityRequest(

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

        boolean hasAirport,

        @NotNull(message = "Vui lòng cho biết thành phố có đang hoạt động hay không")
        Boolean active) {

    public UpdateCityRequest {
        name = InputNormalizer.trim(name);
        nameVi = InputNormalizer.blankToNull(nameVi);
        region = InputNormalizer.blankToNull(region);
        timezone = InputNormalizer.trim(timezone);
    }
}
