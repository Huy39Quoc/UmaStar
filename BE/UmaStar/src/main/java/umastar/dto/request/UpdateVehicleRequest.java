package umastar.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import umastar.enums.VehicleStatus;
import umastar.enums.VehicleType;
import umastar.util.InputNormalizer;

import java.util.Locale;

/**
 * Yêu cầu cập nhật toàn bộ thông tin phương tiện ({@code PUT /api/v1/vehicles/{id}}).
 * {@code status} chỉ được đổi qua lại giữa {@code AVAILABLE} và {@code MAINTENANCE}; {@code IN_USE} do hệ thống
 * tự cập nhật theo chuyến nên không được đặt thủ công (kiểm tra ở tầng service).
 */
public record UpdateVehicleRequest(

        @NotNull(message = "Vui lòng chọn loại phương tiện")
        VehicleType vehicleType,

        @NotBlank(message = "Vui lòng nhập biển số hoặc mã phương tiện")
        @Size(max = 50, message = "Biển số hoặc mã phương tiện tối đa 50 ký tự")
        String codeOrPlate,

        @Min(value = 1, message = "Sức chứa phải từ 1 con ngựa trở lên")
        int capacityHorses,

        @Size(max = 200, message = "Tên đơn vị vận chuyển tối đa 200 ký tự")
        String carrierName,

        @NotNull(message = "Vui lòng chọn trạng thái phương tiện")
        VehicleStatus status) {

    public UpdateVehicleRequest {
        String code = InputNormalizer.trim(codeOrPlate);
        codeOrPlate = code == null ? null : code.toUpperCase(Locale.ROOT);
        carrierName = InputNormalizer.blankToNull(carrierName);
    }
}
