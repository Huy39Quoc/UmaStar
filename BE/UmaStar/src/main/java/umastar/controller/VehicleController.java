package umastar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import umastar.dto.request.CreateVehicleRequest;
import umastar.dto.request.UpdateVehicleRequest;
import umastar.dto.response.PageResponse;
import umastar.dto.response.VehicleResponse;
import umastar.enums.VehicleStatus;
import umastar.enums.VehicleType;
import umastar.service.VehicleService;

import java.util.UUID;

/** API quản lý phương tiện vận chuyển (chỉ dành cho Fleet Coordinator). */
@RestController
@RequestMapping("/api/v1/vehicles")
@PreAuthorize("hasRole('FLEET_COORDINATOR')")
@Tag(name = "Vehicles", description = "Quản lý phương tiện vận chuyển — [Chỉ FLEET_COORDINATOR]")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    /** Danh sách phương tiện có lọc theo từ khóa (biển số/mã, đơn vị vận chuyển), loại, trạng thái và phân trang. */
    @Operation(summary = "[FLEET_COORDINATOR] Danh sách phương tiện",
            description = "Quyền truy cập: Chỉ FLEET_COORDINATOR.")
    @GetMapping
    public PageResponse<VehicleResponse> searchVehicles(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) VehicleType vehicleType,
            @RequestParam(required = false) VehicleStatus status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort) {
        return vehicleService.searchVehicles(keyword, vehicleType, status, page, size, sort);
    }

    /** Xem chi tiết một phương tiện theo ID. */
    @Operation(summary = "[FLEET_COORDINATOR] Chi tiết phương tiện",
            description = "Quyền truy cập: Chỉ FLEET_COORDINATOR.")
    @GetMapping("/{id}")
    public VehicleResponse getVehicle(@PathVariable UUID id) {
        return vehicleService.getVehicle(id);
    }

    /** Tạo phương tiện mới (trạng thái ban đầu AVAILABLE): biển số/mã không trùng, sức chứa từ 1 con trở lên. */
    @Operation(summary = "[FLEET_COORDINATOR] Tạo phương tiện",
            description = "Quyền truy cập: Chỉ FLEET_COORDINATOR.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleResponse createVehicle(@Valid @RequestBody CreateVehicleRequest request) {
        return vehicleService.createVehicle(request);
    }

    /** Cập nhật toàn bộ thông tin; trạng thái chỉ đổi qua lại AVAILABLE và MAINTENANCE, không đổi được khi đang IN_USE. */
    @Operation(summary = "[FLEET_COORDINATOR] Cập nhật phương tiện",
            description = "Quyền truy cập: Chỉ FLEET_COORDINATOR.")
    @PutMapping("/{id}")
    public VehicleResponse updateVehicle(@PathVariable UUID id,
                                         @Valid @RequestBody UpdateVehicleRequest request) {
        return vehicleService.updateVehicle(id, request);
    }

    /** Xóa phương tiện (xóa thật); không xóa được nếu đang IN_USE hoặc đã từng gắn với chuyến nào. */
    @Operation(summary = "[FLEET_COORDINATOR] Xóa phương tiện",
            description = "Quyền truy cập: Chỉ FLEET_COORDINATOR.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVehicle(@PathVariable UUID id) {
        vehicleService.deleteVehicle(id);
    }
}
