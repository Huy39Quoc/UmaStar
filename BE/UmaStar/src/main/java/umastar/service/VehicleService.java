package umastar.service;

import umastar.dto.request.CreateVehicleRequest;
import umastar.dto.request.UpdateVehicleRequest;
import umastar.dto.response.PageResponse;
import umastar.dto.response.VehicleResponse;
import umastar.enums.VehicleStatus;
import umastar.enums.VehicleType;

import java.util.UUID;

/** Nghiệp vụ quản lý phương tiện vận chuyển (Fleet Coordinator). */
public interface VehicleService {

    /** Tìm kiếm / phân trang phương tiện theo từ khóa (biển số/mã, đơn vị vận chuyển), loại và trạng thái. */
    PageResponse<VehicleResponse> searchVehicles(String keyword, VehicleType vehicleType, VehicleStatus status,
                                                 Integer page, Integer size, String sort);

    /** @throws umastar.exception.AppException VEHICLE_NOT_FOUND */
    VehicleResponse getVehicle(UUID vehicleId);

    /**
     * Tạo phương tiện mới ở trạng thái {@code AVAILABLE}.
     *
     * @throws umastar.exception.AppException VEHICLE_CODE_ALREADY_EXISTS nếu biển số/mã đã dùng
     */
    VehicleResponse createVehicle(CreateVehicleRequest request);

    /**
     * Cập nhật toàn bộ thông tin phương tiện. Trạng thái chỉ được đổi qua lại {@code AVAILABLE} và {@code MAINTENANCE}:
     * {@code IN_USE} do hệ thống tự cập nhật theo chuyến, không đặt thủ công và không thể rời khỏi bằng tay.
     *
     * @throws umastar.exception.AppException VEHICLE_NOT_FOUND, VEHICLE_CODE_ALREADY_EXISTS,
     *                                        VEHICLE_IN_USE (đang IN_USE mà chuyển MAINTENANCE),
     *                                        INVALID_STATE_TRANSITION (các chuyển trạng thái không hợp lệ còn lại)
     */
    VehicleResponse updateVehicle(UUID vehicleId, UpdateVehicleRequest request);

    /**
     * Xóa phương tiện (xóa thật). Không xóa được nếu đang {@code IN_USE} hoặc đã từng gắn với bất kỳ chuyến nào.
     *
     * @throws umastar.exception.AppException VEHICLE_NOT_FOUND, VEHICLE_IN_USE
     */
    void deleteVehicle(UUID vehicleId);
}
