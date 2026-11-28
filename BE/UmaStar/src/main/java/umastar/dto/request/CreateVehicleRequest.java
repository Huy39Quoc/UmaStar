package umastar.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import umastar.enums.VehicleType;
import umastar.util.InputNormalizer;

import java.util.Locale;

/**
 * Yêu cầu tạo phương tiện ({@code POST /api/v1/vehicles}). Phương tiện mới luôn ở trạng thái {@code AVAILABLE}.
 * Biển số / mã được cắt khoảng trắng và viết hoa để việc kiểm tra trùng không phụ thuộc hoa thường.
 */
public record CreateVehicleRequest(

        @NotNull(message = "Vui lòng chọn loại phương tiện")
        VehicleType vehicleType,

        @NotBlank(message = "Vui lòng nhập biển số hoặc mã phương tiện")
        @Size(max = 50, message = "Biển số hoặc mã phương tiện tối đa 50 ký tự")
        String codeOrPlate,

        @Min(value = 1, message = "Sức chứa phải từ 1 con ngựa trở lên")
        int capacityHorses,

        @Size(max = 200, message = "Tên đơn vị vận chuyển tối đa 200 ký tự")
        String carrierName) {

    public CreateVehicleRequest {
        String code = InputNormalizer.trim(codeOrPlate);
        codeOrPlate = code == null ? null : code.toUpperCase(Locale.ROOT);
        carrierName = InputNormalizer.blankToNull(carrierName);
    }
}
